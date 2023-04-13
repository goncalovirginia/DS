package servers.resources;

import api.Message;
import api.User;
import api.java.Feeds;
import api.java.Result;
import api.java.Result.ErrorCode;
import clients.FeedsClientFactory;
import clients.UsersClientFactory;
import discovery.DiscoverySingleton;
import servers.Server;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

public class FeedsResource implements Feeds {
	
	private static final Logger Log = Logger.getLogger(FeedsResource.class.getName());
	
	private final Map<String, Map<Long, Message>> userFeed;
	private final Map<String, Set<String>> userSubscribers, userSubscribedTo;
	
	public FeedsResource() {
		userFeed = new ConcurrentHashMap<>();
		userSubscribers = new ConcurrentHashMap<>();
		userSubscribedTo = new ConcurrentHashMap<>();
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		Log.info("postMessage : user = " + user + "; pwd = " + pwd + "; msg = " + msg);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		if (msg.getUser() == null || !msg.getUser().equals(nameAndDomain[0]) || msg.getDomain() == null || !msg.getDomain().equals(Server.domain)) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		Message newMsg = new Message(msg);
		userFeed.putIfAbsent(user, new ConcurrentHashMap<>());
		userFeed.get(user).put(newMsg.getId(), newMsg);
		
		propagateMessage(newMsg);
		propagateMessageToOtherDomains(newMsg);
		
		return Result.ok(newMsg.getId());
	}
	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		Log.info("removeFromPersonalFeed : user = " + user + "; mid = " + mid + "; pwd = " + pwd);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		Map<Long, Message> feed = userFeed.get(user);
		if (feed == null) return Result.error(ErrorCode.NOT_FOUND);
		Message removedMsg = feed.remove(mid);
		if (removedMsg == null) return Result.error(ErrorCode.NOT_FOUND);
		
		return Result.ok();
	}
	
	@Override
	public Result<Message> getMessage(String user, long mid) {
		Log.info("getMessage : user = " + user + "; mid = " + mid);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(nameAndDomain[1] + ":feeds")).getMessage(user, mid);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
		if (userResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);
		
		Map<Long, Message> feed = userFeed.get(user);
		if (feed == null) return Result.error(ErrorCode.NOT_FOUND);
		Message msg = feed.get(mid);
		if (msg == null) return Result.error(ErrorCode.NOT_FOUND);
		
		return Result.ok(msg);
	}
	
	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		Log.info("getMessages : user = " + user + "; time = " + time);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(nameAndDomain[1] + ":feeds")).getMessages(user, time);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
		if (userResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		userFeed.putIfAbsent(user, new ConcurrentHashMap<>());
		List<Message> messages = new LinkedList<>();
		
		for (Message message : userFeed.get(user).values()) {
			if (message.getCreationTime() > time) {
				messages.add(message);
			}
		}
		
		return Result.ok(messages);
	}
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		Log.info("subUser : user = " + user + "; userSub = " + userSub + "; pwd = " + pwd);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		String[] subNameAndDomain = userSub.split("@");
		
		Result<User> subUserResult = validateUserCredentials(subNameAndDomain[1], subNameAndDomain[0], "");
		if (subUserResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		userSubscribers.putIfAbsent(userSub, new ConcurrentSkipListSet<>());
		userSubscribers.get(userSub).add(user);
		userSubscribedTo.putIfAbsent(user, new ConcurrentSkipListSet<>());
		userSubscribedTo.get(user).add(userSub);
		
		return Result.ok();
	}
	
	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		Log.info("unsubscribeUser : user = " + user + "; userSub = " + userSub + "; pwd = " + pwd);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		String[] subNameAndDomain = userSub.split("@");
		
		Result<User> subUserResult = validateUserCredentials(subNameAndDomain[1], subNameAndDomain[0], "");
		if (subUserResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		userSubscribers.putIfAbsent(userSub, new HashSet<>());
		userSubscribers.get(userSub).remove(user);
		userSubscribedTo.putIfAbsent(user, new HashSet<>());
		userSubscribedTo.get(user).remove(userSub);
		
		return Result.ok();
	}
	
	@Override
	public Result<List<String>> listSubs(String user) {
		Log.info("listSubs : user = " + user);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) {
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
		if (userResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		userSubscribedTo.putIfAbsent(user, new HashSet<>());
		
		return Result.ok(userSubscribedTo.get(user).stream().toList());
	}
	
	@Override
	public Result<Void> propagateMessage(Message message) {
		Log.info("propagateMessage : message = " + message);
		
		Set<String> subscribers = userSubscribers.get(message.getUser() + "@" + message.getDomain());
		
		if (subscribers != null) {
			for (String subscriber : subscribers) {
				userFeed.putIfAbsent(subscriber, new ConcurrentHashMap<>());
				userFeed.get(subscriber).put(message.getId(), message);
			}
		}
		
		return Result.ok();
	}
	
	public Result<Void> deleteUserData(String user) {
		Log.info("deleteUserData : user = " + user);
		
		userFeed.remove(user);
		userSubscribers.remove(user);
		userSubscribedTo.remove(user);
		
		return Result.ok();
	}
	
	private Result<User> validateUserCredentials(String domain, String userId, String password) {
		URI uri = DiscoverySingleton.getInstance().getURI(domain + ":users");
		return UsersClientFactory.get(uri).getUser(userId, password);
	}
	
	private void propagateMessageToOtherDomains(Message message) {
		for (URI uri : DiscoverySingleton.getInstance().getURIsOfOtherDomainsFeeds(Server.domain)) {
			FeedsClientFactory.get(uri).propagateMessage(message);
		}
	}
	
}
