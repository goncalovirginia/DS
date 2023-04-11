package servers.resources;

import api.Message;
import api.User;
import api.java.Feeds;
import api.java.Result;
import clients.FeedsClientFactory;
import clients.UsersClientFactory;
import discovery.DiscoverySingleton;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FeedsResource implements Feeds {
	
	private static final Logger Log = Logger.getLogger(FeedsResource.class.getName());
	
	private final Map<String, Map<Long, Message>> userFeed;
	private final Map<String, Set<String>> userSubscribers;
	
	public FeedsResource() {
		userFeed = new ConcurrentHashMap<>();
		userSubscribers = new ConcurrentHashMap<>();
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		if (msg.getUser() == null || msg.getDomain() == null || msg.getText() == null) {
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
		
		String[] nameAndDomain = user.split("@");
		
		Result<User> userResult = validateUserCredentials(nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);
		
		Message newMsg = new Message(msg);
		userFeed.get(user).put(newMsg.getId(), newMsg);
		
		propagateMessage(newMsg);
		
		for (URI uri : DiscoverySingleton.getInstance().knownURIsOf("feeds", 1)) {
			FeedsClientFactory.get(uri).propagateMessage(newMsg);
		}
		
		return Result.ok(newMsg.getId());
	}
	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		String[] nameAndDomain = user.split("@");
		
		Result<User> userResult = validateUserCredentials(nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);
		
		Message removedMsg = userFeed.get(nameAndDomain[0]).remove(mid);
		if (removedMsg == null) return Result.error(Result.ErrorCode.NOT_FOUND);
		
		return Result.ok();
	}
	
	@Override
	public Result<Message> getMessage(String user, long mid) {
		String[] nameAndDomain = user.split("@");
		
		Message msg = userFeed.get(nameAndDomain[0]).get(mid);
		if (msg == null) return Result.error(Result.ErrorCode.NOT_FOUND);
		
		return Result.ok(msg);
	}
	
	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		return Result.ok();
	}
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		return Result.ok();
	}
	
	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		return Result.ok();
	}
	
	@Override
	public Result<List<String>> listSubs(String user) {
		return Result.ok();
	}
	
	@Override
	public Result<Void> propagateMessage(Message message) {
		Set<String> subscribers = userSubscribers.get(message.getUser());
		
		if (subscribers != null) {
			for (String subscriber : subscribers) {
				userFeed.get(subscriber).put(message.getId(), message);
			}
		}
		
		return Result.ok();
	}
	
	private Result<User> validateUserCredentials(String userId, String password) {
		try {
			return UsersClientFactory.get(new URI("")).getUser(userId, password);
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
	}
	
}
