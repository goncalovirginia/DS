package servers.soap;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import discovery.DiscoverySingleton;
import jakarta.xml.ws.Endpoint;
import servers.Server;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public abstract class SoapServer extends Server {
	
	public static final String SERVER_BASE_URI = "https://%s:%s/soap";
	
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
			String hostName = InetAddress.getLocalHost().getHostName();
			
			HttpsServer server = HttpsServer.create(new InetSocketAddress(hostName, port), 0);
			server.setExecutor(Executors.newCachedThreadPool());
			server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
			
			Endpoint endpoint = Endpoint.create(resource.getConstructor().newInstance());
			endpoint.publish(server.createContext("/soap"));
			
			server.start();
			
			String serverURI = String.format(SERVER_BASE_URI, hostName, port);
			
			Log.info(String.format("%s Soap Server ready @ %s\n", domain + ":" + service, serverURI));
			
			DiscoverySingleton.getInstance().announce(domain + ":" + service, serverURI);
		} catch (Exception e) {
			Log.info(e.getMessage());
		}
	}
	
}
