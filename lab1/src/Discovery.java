import java.net.URI;
import java.util.List;

/**
 * An interface to perform service discovery based on periodic
 * announcements over multicast communication.
 */

public interface Discovery {
	
	/**
	 * Used to announce the URI of the given service name.
	 * @param serviceName - the name of the service
	 * @param serviceURI - the uri of the service
	 */
	void announce(String serviceName, String serviceURI);
	
	/**
	 * Get discovered URIs for a given service name
	 * @param serviceName - name of the service
	 * @param minReplies - minimum number of requested URIs. Blocks until the number is satisfied.
	 * @return array with the discovered URIs for the given service name.
	 */
	List<URI> knownURIsOf(String serviceName, int minReplies);
	
}
