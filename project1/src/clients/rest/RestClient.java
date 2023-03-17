package clients.rest;

import api.Result;
import clients.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;

public abstract class RestClient extends Client {
	
	protected final URI serverURI;
	protected final jakarta.ws.rs.client.Client client;
	protected final ClientConfig config;
	protected WebTarget target;
	
	protected RestClient(URI serverURI, String path) {
		this.serverURI = serverURI;
		
		config = new ClientConfig();
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		
		client = ClientBuilder.newClient(config);
		
		target = client.target(serverURI).path(path);
	}
	
	protected <T> Result<T> responseToResult(Response r, Class<T> desiredClass) {
		if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
			return Result.ok(r.readEntity(desiredClass));
		}
		if (r.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return Result.ok();
		}
		return Result.error(Result.ErrorCode.valueOf(Response.Status.fromStatusCode(r.getStatus()).name()));
	}
	
	protected <T> Result<T> responseToResult(Response r, GenericType<T> desiredType) {
		if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
			return Result.ok(r.readEntity(desiredType));
		}
		return Result.error(Result.ErrorCode.valueOf(Response.Status.fromStatusCode(r.getStatus()).name()));
	}
	
}
