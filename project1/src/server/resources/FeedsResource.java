package server.resources;

import api.Message;
import api.User;
import api.rest.FeedsService;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class FeedsResource implements FeedsService {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private static final Map<String, User> feeds = new HashMap<>();
	
	public FeedsResource() {
	
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
