import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

/**
 * Basic TCP client...
 */
public class TcpClient {
	
	private static final String QUIT = "!quit";
	
	public static void main(String[] args) throws Exception {
		URI uri = DiscoverySingleton.getInstance().knownURIsOf("service1", 1).get(0);
		String hostname = uri.getHost();
		int port = uri.getPort();
		
		System.out.println("Connected with server at: " + uri);
		
		try (var cs = new Socket(hostname, port); var sc = new Scanner(System.in)) {
			String input;
			do {
				input = sc.nextLine();
				cs.getOutputStream().write((input + System.lineSeparator()).getBytes());
			} while (!input.equals(QUIT));
			
		}
	}
}
