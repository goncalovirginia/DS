package servers.soap;

import discovery.DiscoverySingleton;
import jakarta.xml.ws.Endpoint;
import servers.Server;

import java.net.InetAddress;

abstract class SoapServer extends Server {
	
	public static final String SERVER_BASE_URI = "http://%s:%s/soap";
	
	static {
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
	}
	
	protected SoapServer(String className, int port, String service, Class<?> resource) {
		super(className, port, service, resource);
	}
	
	protected void run() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_BASE_URI, ip, port);
			
			Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), resource.getConstructor().newInstance());
			
			Log.info(String.format("%s Soap Server ready @ %s\n", domain + ":" + service, serverURI));
			
			DiscoverySingleton.getInstance().announce(domain + ":" + service, serverURI);
		}
		catch (Exception e) {
			Log.info(e.getMessage());
		}
	}
	
}
