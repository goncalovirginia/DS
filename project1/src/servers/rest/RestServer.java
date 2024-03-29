package servers.rest;

import discovery.DiscoverySingleton;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import servers.Server;

import java.net.InetAddress;
import java.net.URI;

abstract class RestServer extends Server {

	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	protected RestServer(String className, int port, String service, Class<?> resource) {
		super(className, port, service, resource);
	}

	protected void run() {
		try {
			ResourceConfig config = new ResourceConfig();
			config.register(resource);

			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, port);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

			Log.info(String.format("%s Server ready @ %s\n", domain + ":" + service, serverURI));

			DiscoverySingleton.getInstance().announce(domain + ":" + service, serverURI);
		} catch (Exception e) {
			Log.info(e.getMessage());
		}
	}

}
