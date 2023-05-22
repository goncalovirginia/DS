package clients;

import api.java.Feeds;
import clients.rest.FeedsRestClient;
import clients.soap.FeedsSoapClient;

import java.net.URI;

public class FeedsClientFactory {

	private static final String REST = "/rest";
	private static final String SOAP = "/soap";

	public static Feeds get(URI serverURI) {
		String uriString = serverURI.toString();

		if (uriString.endsWith(REST)) {
			return new FeedsRestClient(serverURI);
		}
		if (uriString.endsWith(SOAP)) {
			return new FeedsSoapClient(serverURI);
		}

		throw new RuntimeException("Unknown service type..." + uriString);
	}

}
