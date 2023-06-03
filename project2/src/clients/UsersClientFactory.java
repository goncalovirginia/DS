package clients;

import api.java.Users;
import clients.rest.UsersRestClient;
import clients.soap.UsersSoapClient;

import java.net.URI;

public class UsersClientFactory {

	private static final String REST = "/rest";
	private static final String SOAP = "/soap";

	public static Users get(URI serverURI) {
		String uriString = serverURI.toString();

		if (uriString.endsWith(REST)) {
			return new UsersRestClient(serverURI);
		}
		if (uriString.endsWith(SOAP)) {
			return new UsersSoapClient(serverURI);
		}

		throw new RuntimeException("Unknown service type..." + uriString);
	}

}
