package servers;

import java.util.logging.Logger;

public abstract class Server {
	
	protected static Logger Log;
	protected final int port;
	protected final String service;
	protected final Class<?> resource;
	
	public static String domain;
	public static long serverId;
	public static String secret;
	public static String serverURI;
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	protected Server(String className, int port, String service, Class<?> resource) {
		Log = Logger.getLogger(className);
		this.port = port;
		this.service = service;
		this.resource = resource;
	}
	
}
