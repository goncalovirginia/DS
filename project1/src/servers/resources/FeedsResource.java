package servers.resources;

import api.Message;
import api.User;
import api.rest.RestFeeds;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FeedsResource implements Feeds {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final Map<String, User> feeds;
	
	public FeedsResource() {
		feeds = new HashMap<>();
	}
	
	@Override
	public long postMessage(String user, String domain, String pwd, Message msg) {
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
	public void subUser(String user, long userSub, String pwd) {
	
	}
	
	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
	
	}
	
	@Override
	public List<User> listSubs(String user) {
		return null;
	}
}
