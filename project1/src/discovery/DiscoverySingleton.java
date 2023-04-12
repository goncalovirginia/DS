package discovery;

import java.net.*;
import java.util.*;
import java.util.logging.Logger;

public class DiscoverySingleton implements Discovery {
	
	private static final Logger Log = Logger.getLogger(Discovery.class.getName());
	
	// The pre-agreed multicast endpoint assigned to perform discovery.
	public static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	
	public static final int DISCOVERY_RETRY_TIMEOUT = 5000;
	public static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;
	
	private static final String DELIMITER = "\t";
	private static final int MAX_DATAGRAM_SIZE = 65536;
	
	private static Discovery singleton;
	private static final Map<String, Set<URI>> serviceURIs = new HashMap<>();
	
	public synchronized static Discovery getInstance() {
		if (singleton == null) {
			singleton = new DiscoverySingleton();
		}
		return singleton;
	}
	
	private DiscoverySingleton() {
		startListener();
	}
	
	@Override
	public void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", DISCOVERY_ADDR, serviceName, serviceURI));
		
		byte[] packetBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
		DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, DISCOVERY_ADDR);
		
		// start thread to send periodic announcements
		new Thread(() -> {
			try (var ds = new DatagramSocket()) {
				while (true) {
					try {
						ds.send(packet);
						Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public List<URI> knownURIsOf(String serviceName, int minEntries) {
		Set<URI> uris;
		while ((uris = serviceURIs.get(serviceName)) == null || uris.size() < minEntries) ;
		return uris.stream().toList();
	}
	
	private void startListener() {
		Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n", DISCOVERY_ADDR.getAddress(), DISCOVERY_ADDR.getPort()));
		
		new Thread(() -> {
			try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
				ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
				
				while (true) {
					try {
						var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
						ms.receive(pkt);
						var msg = new String(pkt.getData(), 0, pkt.getLength());
						Log.info(String.format("Received: %s", msg));
						String[] parts = msg.split(DELIMITER);
						
						if (parts.length != 2) {
							throw new Exception("Invalid announcement format.");
						}
						
						serviceURIs.putIfAbsent(parts[0], new HashSet<>());
						serviceURIs.get(parts[0]).add(URI.create(parts[1]));
					}
					catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
			catch (Exception x) {
				x.printStackTrace();
			}
		}).start();
	}
}
