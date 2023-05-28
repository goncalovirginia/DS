package servers.rest;

public class UsersRestServer extends RestServer {
	
	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	
	private UsersRestServer() {
		super(UsersRestServer.class.getName(), PORT, SERVICE, UsersRestResource.class);
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			Log.info("Arguments: <Domain> <Secret>");
		}
		
		domain = args[0];
		secret = args[1];
		
		new UsersRestServer().run();
	}
	
}
