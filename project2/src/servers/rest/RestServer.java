package servers.rest;

import discovery.DiscoverySingleton;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import servers.Server;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.URI;

public abstract class RestServer extends Server {

	private static final String SERVER_URI_FMT = "https://%s:%s/rest";

	protected RestServer(String className, int port, String service, Class<?> resource) {
		super(className, port, service, resource);
	}

	protected void run() {
		runConfig(new ResourceConfig().register(resource));
	}

	protected void run(Object... resourceInstances) {
		ResourceConfig config = new ResourceConfig();

		for (Object instance : resourceInstances) {
			config.register(instance);
		}

		runConfig(config);
	}

	private void runConfig(ResourceConfig config) {
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			serverURI = String.format(SERVER_URI_FMT, hostName, port);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Log.info(String.format("%s Server ready @ %s\n", domain + ":" + service, serverURI));

			DiscoverySingleton.getInstance().announce(domain + ":" + service, serverURI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
