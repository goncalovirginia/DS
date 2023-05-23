package servers.soap;

public class FeedsSoapServer extends SoapServer {

	public static final int PORT = 8080;
	public static final String SERVICE = "feeds";

	private FeedsSoapServer() {
		super(FeedsSoapServer.class.getName(), PORT, SERVICE, FeedsSoapResource.class);
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			Log.info("Arguments: <Domain> <ServerId>");
		}

		domain = args[0];
		serverId = Long.parseLong(args[1]);

		new FeedsSoapServer().run();
	}

}
