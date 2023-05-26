package clients.rest;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.rest.RestFeeds;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import zookeeper.FeedsOperation;

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
		return reTry(() -> clt_getMessage(user, mid));
	}

	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		return reTry(() -> clt_getMessages(user, time));
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
	public Result<Void> propagateMessage(Message message, String secret) {
		return reTry(() -> clt_propagateMessage(message, secret));
	}

	@Override
	public Result<Void> deleteUserData(String user, String secret) {
		return reTry(() -> clt_deleteUserData(user, secret));
	}

	@Override
	public Result<Void> replicateOperation(FeedsOperation operation, String secret) {
		return reTry(() -> clt_replicateOperation(operation, secret));
	}

	private Result<Void> clt_replicateOperation(FeedsOperation operation, String secret) {
		Response r = target.path("replicateOperation")
				.queryParam("secret", secret)
				.request()
				.put(Entity.entity(operation, MediaType.APPLICATION_JSON));

		return responseToResult(r, Void.class);
	}

	private Result<Void> clt_propagateMessage(Message message, String secret) {
		Response r = target.path("propagate")
				.queryParam("secret", secret)
				.request()
				.put(Entity.entity(message, MediaType.APPLICATION_JSON));

		return responseToResult(r, Void.class);
	}

	private Result<Message> clt_getMessage(String user, long mid) {
		Response r = target.path(user)
				.path(Long.toString(mid))
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return responseToResult(r, Message.class);
	}

	private Result<List<Message>> clt_getMessages(String user, long time) {
		Response r = target.path(user)
				.queryParam(Long.toString(time))
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return responseToResult(r, new GenericType<List<Message>>() {
		});
	}

	private Result<Void> clt_deleteUserData(String user, String secret) {
		Response r = target.path("deleteData")
				.path(user)
				.queryParam("secret", secret)
				.request()
				.delete();

		return responseToResult(r, Void.class);
	}

}
