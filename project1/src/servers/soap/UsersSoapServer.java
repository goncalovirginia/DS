package servers.soap;

public class UsersSoapServer extends SoapServer {
	
	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	
	private UsersSoapServer() {
		super(UsersSoapServer.class.getName(), PORT, SERVICE, UsersSoapResource.class);
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			Log.info("Arguments: <Domain> <ServerId>");
		}
		
		domain = args[0];
		serverId = Long.parseLong(args[1]);
		
		new UsersSoapServer().run();
	}
	
}
