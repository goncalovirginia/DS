package discovery;

import java.net.URI;
import java.util.List;

/**
 * An interface to perform service discovery based on periodic
 * announcements over multicast communication.
 */

public interface Discovery {
	
	/**
	 * Used to announce the URI of the given service name.
	 *
	 * @param serviceName - the name of the service
	 * @param serviceURI  - the uri of the service
	 */
	void announce(String serviceName, String serviceURI);
	
	/**
	 * Get the URI for a given service name
	 *
	 * @param service - name of the service
	 * @return URI for the given service name.
	 */
	URI getURI(String service);
	
	/**
	 * Get the URIs of feeds services from other domains.
	 *
	 * @param currDomain domain calling the method (not included in the return value)
	 * @return all feeds service URIs from other domains.
	 */
	List<URI> getURIsOfOtherDomainsFeeds(String currDomain);
	
}
