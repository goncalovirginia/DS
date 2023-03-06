import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Basic TCP server...
 */
public class TcpServer {
	
	private static final int PORT = 9000;
	private static final int BUF_SIZE = 1024;
	private static final String URI_FORMAT = "tcp://%s:%s";
	
	public static void main(String[] args) throws Exception {
		String uri = String.format(URI_FORMAT, InetAddress.getLocalHost().getHostAddress(), PORT);
		DiscoverySingleton.getInstance().announce("service1", uri);
		System.out.println("Accepting connections at: " + uri);
		
		try (var ss = new ServerSocket(PORT)) {
			while (true) {
				Socket cs = ss.accept();
				System.out.println("Accepted connection from client at: " + cs.getRemoteSocketAddress());
				
				byte[] buf = new byte[BUF_SIZE];
				int bytesRead;
				while ((bytesRead = cs.getInputStream().read(buf)) > 0)
					System.out.write(buf, 0, bytesRead);
				
				System.err.println("Connection closed.");
			}
		}
	}
	
}
