package servers.rest;

public class FeedsProxyRestServer extends RestServer {

	public static final int PORT = 8080;
	public static final String SERVICE = "feeds";

	private FeedsProxyRestServer() {
		super(FeedsProxyRestServer.class.getName(), PORT, SERVICE, FeedsProxyRestResource.class);
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			Log.info("Arguments: <Domain> <ServerId> <Secret>");
		}

		domain = args[0];
		serverId = Long.parseLong(args[1]);
		secret = args[2];

		new FeedsProxyRestServer().run();
	}

}
