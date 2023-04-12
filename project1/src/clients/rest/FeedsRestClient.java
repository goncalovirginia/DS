package clients.rest;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.rest.RestFeeds;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

public class FeedsRestClient extends RestClient implements Feeds {
	
	public FeedsRestClient(URI serverURI) {
		super(serverURI, RestFeeds.PATH);
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		return null;
	}
	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		return null;
	}
	
	@Override
	public Result<Message> getMessage(String user, long mid) {
		return null;
	}
	
	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		return null;
	}
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		return null;
	}
	
	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		return null;
	}
	
	@Override
	public Result<List<String>> listSubs(String user) {
		return null;
	}
	
	@Override
	public Result<Void> propagateMessage(Message message) {
		return reTry(() -> clt_propagateMessage(message));
	}
	
	private Result<Void> clt_propagateMessage(Message message) {
		Response r = target.path("propagate")
				.request()
				.put(Entity.entity(message, MediaType.APPLICATION_JSON));
		
		return responseToResult(r, Void.class);
	}
	
}
