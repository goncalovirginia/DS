package servers.rest;

import api.Message;
import api.User;
import api.rest.RestFeeds;
import jakarta.inject.Singleton;
import servers.resources.FeedsResource;

import java.util.List;

@Singleton
public class FeedsRestResource implements RestFeeds {
	
	private final FeedsResource feeds;
	
	public FeedsRestResource() {
		feeds = new FeedsResource();
	}
	
	@Override
	public long postMessage(String user, String pwd, Message msg) {
		return RestResource.processResult(feeds.postMessage(user, pwd, msg));
	}
	
	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) {
		RestResource.processResult(feeds.removeFromPersonalFeed(user, mid, pwd));
	}
	
	@Override
	public Message getMessage(String user, long mid) {
		return RestResource.processResult(feeds.getMessage(user, mid));
	}
	
	@Override
	public List<Message> getMessages(String user, long time) {
		return RestResource.processResult(feeds.getMessages(user, time));
	}
	
	@Override
	public void subUser(String user, String userSub, String pwd) {
		RestResource.processResult(feeds.subUser(user, userSub, pwd));
	}
	
	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
		RestResource.processResult(feeds.unsubscribeUser(user, userSub, pwd));
	}
	
	@Override
	public List<User> listSubs(String user) {
		return RestResource.processResult(feeds.listSubs(user));
	}
	
}
