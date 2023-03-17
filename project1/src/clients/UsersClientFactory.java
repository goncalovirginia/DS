package clients;

import api.Users;
import clients.rest.UsersRestClient;
import clients.soap.UsersSoapClient;
import discovery.DiscoverySingleton;
import servers.rest.UsersRestServer;

import java.io.IOException;
import java.net.URI;

public class UsersClientFactory {
	
	public static Users getClient() throws IOException {
		URI serverURI = DiscoverySingleton.getInstance().knownURIsOf(UsersRestServer.SERVICE, 1).get(0);
		
		if (serverURI.toString().endsWith("rest")) {
			return new UsersRestClient(serverURI);
		}
		
		return new UsersSoapClient(serverURI);
	}
	
}
