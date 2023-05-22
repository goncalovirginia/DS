package servers.rest;

import api.Message;
import api.java.Feeds;
import api.rest.RestFeeds;
import servers.resources.FeedsResource;

import java.util.List;

public class FeedsReplicatedRestResource extends RestResource implements RestFeeds {

	protected Feeds feeds;

	public FeedsReplicatedRestResource() {
		feeds = new FeedsResource();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) {
		return 0;
	}

	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) {

	}

	@Override
	public Message getMessage(String user, long mid) {
		return null;
	}

	@Override
	public List<Message> getMessages(String user, long time) {
		return null;
	}

	@Override
	public void subUser(String user, String userSub, String pwd) {

	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {

	}

	@Override
	public List<String> listSubs(String user) {
		return null;
	}

	@Override
	public void propagateMessage(Message message, String secret) {

	}

	@Override
	public void deleteUserData(String user, String secret) {

	}
}
