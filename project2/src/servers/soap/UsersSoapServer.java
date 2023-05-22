package servers.soap;

public class UsersSoapServer extends SoapServer {

	public static final int PORT = 8080;
	public static final String SERVICE = "users";

	private UsersSoapServer() {
		super(UsersSoapServer.class.getName(), PORT, SERVICE, UsersSoapResource.class);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			Log.info("Arguments: <Domain>");
		}

		domain = args[0];

		new UsersSoapServer().run();
	}

}
