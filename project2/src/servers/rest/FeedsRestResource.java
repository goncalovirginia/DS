package servers.rest;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.rest.RestFeeds;
import jakarta.inject.Singleton;
import servers.resources.FeedsResource;
import zookeeper.FeedsOperation;

import java.util.List;

@Singleton
public class FeedsRestResource extends RestResource implements RestFeeds {

	protected Feeds feeds;

	public FeedsRestResource() {
		feeds = new FeedsResource();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) {
		return fromJavaResult(feeds.postMessage(user, pwd, msg));
	}

	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) {
		fromJavaResult(feeds.removeFromPersonalFeed(user, mid, pwd));
	}

	@Override
	public Message getMessage(long version, String user, long mid) {
		return fromJavaResult(feeds.getMessage(user, mid));
	}

	@Override
	public List<Message> getMessages(long version, String user, long time) {
		return fromJavaResult(feeds.getMessages(user, time));
	}

	@Override
	public void subUser(String user, String userSub, String pwd) {
		fromJavaResult(feeds.subUser(user, userSub, pwd));
	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
		fromJavaResult(feeds.unsubscribeUser(user, userSub, pwd));
	}

	@Override
	public List<String> listSubs(long version, String user) {
		return fromJavaResult(feeds.listSubs(user));
	}

	@Override
	public void propagateMessage(Message message, String secret) {
		fromJavaResult(feeds.propagateMessage(message, secret));
	}

	@Override
	public void deleteUserData(String user, String secret) {
		fromJavaResult(feeds.deleteUserData(user, secret));
	}

	@Override
	public void replicateOperation(FeedsOperation operation, String secret) {
		fromJavaResult(Result.error(Result.ErrorCode.NOT_IMPLEMENTED));
	}

}
