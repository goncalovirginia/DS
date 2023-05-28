package discovery;

import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	private static final Map<String, URI> serviceURI = new HashMap<>();
	
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
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public URI getURI(String service) {
		while (serviceURI.get(service) == null) ;
		return serviceURI.get(service);
	}
	
	@Override
	public List<URI> getURIsOfOtherDomainsFeeds(String currDomain) {
		List<URI> uris = new LinkedList<>();
		
		for (Map.Entry<String, URI> entry : serviceURI.entrySet()) {
			if (!entry.getKey().contains(currDomain) && entry.getKey().contains("feeds")) {
				uris.add(entry.getValue());
			}
		}
		
		return uris;
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
						
						serviceURI.put(parts[0], URI.create(parts[1]));
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}).start();
	}
}
