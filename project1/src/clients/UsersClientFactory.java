package clients;

import api.Users;
import clients.rest.UsersRestClient;
import clients.soap.UsersSoapClient;
import discovery.DiscoverySingleton;
import servers.rest.UsersRestServer;

import java.io.IOException;
import java.net.URI;

public class UsersClientFactory {
	
	private static final String REST = "/rest";
	private static final String SOAP = "/soap";
	
	public static Users get(URI serverURI) {
		var uriString = serverURI.toString();
		
		if (uriString.endsWith(REST))
			return new UsersRestClient(serverURI);
		else if (uriString.endsWith(SOAP))
			return new UsersSoapClient(serverURI);
		else
			throw new RuntimeException("Unknown service type..." + uriString);
	}
	
}
