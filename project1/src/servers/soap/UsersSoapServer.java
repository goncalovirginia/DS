package servers.soap;

public class UsersSoapServer extends SoapServer {
	
	public static final int PORT = 8080;
	public static final String SERVICE_NAME = "users";
	
	private UsersSoapServer() {
		super(UsersSoapResource.class.getName(), PORT, SERVICE_NAME, UsersSoapResource.class);
	}
	
	public static void main(String[] args) {
		new UsersSoapServer().run();
	}
	
}
