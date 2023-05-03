package servers.rest;

public class FeedsRestServer extends RestServer {
	
	public static final int PORT = 8080;
	public static final String SERVICE = "feeds";
	
	private FeedsRestServer() {
		super(FeedsRestServer.class.getName(), PORT, SERVICE, FeedsRestResource.class);
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			Log.info("Arguments: <Domain> <ServerId>");
		}
		
		domain = args[0];
		serverId = Long.parseLong(args[1]);
		
		new FeedsRestServer().run();
	}
	
}
