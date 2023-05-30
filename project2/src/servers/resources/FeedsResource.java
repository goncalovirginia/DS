package servers.resources;

import api.Message;
import api.User;
import api.java.Feeds;
import api.java.Result;
import api.java.Result.ErrorCode;
import clients.FeedsClientFactory;
import clients.UsersClientFactory;
import com.google.gson.reflect.TypeToken;
import discovery.DiscoverySingleton;
import servers.Server;
import utils.JSON;
import zookeeper.FeedsOperation;
import zookeeper.ZookeeperReplicationManager;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FeedsResource implements Feeds {
	
	private static final Logger Log = Logger.getLogger(FeedsResource.class.getName());
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	private final ConcurrentMap<String, ConcurrentMap<Long, Message>> userFeed;
	private final ConcurrentMap<String, Set<String>> userSubscribedTo, userSubscribers;
	
	public FeedsResource() {
		userFeed = new ConcurrentHashMap<>();
		userSubscribedTo = new ConcurrentHashMap<>();
		userSubscribers = new ConcurrentHashMap<>();
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		Log.info("postMessage : user = " + user + "; pwd = " + pwd + "; msg = " + msg);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) return Result.error(ErrorCode.BAD_REQUEST);
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		if (msg.getUser() == null || !msg.getUser().equals(nameAndDomain[0]) || msg.getDomain() == null || !msg.getDomain().equals(Server.domain)) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		msg.create();
		
		synchronized (userFeed) {
			userFeed.putIfAbsent(user, new ConcurrentHashMap<>());
			userFeed.get(user).put(msg.getId(), msg);
		}
		
		propagateMessage(msg, Server.secret);
		propagateMessageToOtherDomains(msg);
		
		return Result.ok(msg.getId());
	}
	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		Log.info("removeFromPersonalFeed : user = " + user + "; mid = " + mid + "; pwd = " + pwd);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) return Result.error(ErrorCode.BAD_REQUEST);
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		Map<Long, Message> feed = userFeed.get(user);
		if (feed == null || feed.remove(mid) == null) return Result.error(ErrorCode.NOT_FOUND);
		
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
		if (userResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		Map<Long, Message> feed = userFeed.get(user);
		Message message;
		if (feed == null || (message = feed.get(mid)) == null) return Result.error(ErrorCode.NOT_FOUND);
		
		return Result.ok(message);
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
		
		Map<Long, Message> feed = userFeed.get(user);
		List<Message> messages = new LinkedList<>();
		
		if (feed != null) {
			for (Message message : feed.values()) {
				if (message.getCreationTime() > time) {
					messages.add(message);
				}
			}
		}
		
		return Result.ok(messages);
	}
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		Log.info("subUser : user = " + user + "; userSub = " + userSub + "; pwd = " + pwd);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) return Result.error(ErrorCode.BAD_REQUEST);
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		String[] subNameAndDomain = userSub.split("@");
		
		Result<User> subUserResult = validateUserCredentials(subNameAndDomain[1], subNameAndDomain[0], "");
		if (subUserResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		synchronized (userSubscribedTo) {
			userSubscribedTo.putIfAbsent(user, ConcurrentHashMap.newKeySet());
			userSubscribedTo.get(user).add(userSub);
		}
		synchronized (userSubscribers) {
			userSubscribers.putIfAbsent(userSub, ConcurrentHashMap.newKeySet());
			userSubscribers.get(userSub).add(user);
		}
		
		return Result.ok();
	}
	
	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		Log.info("unsubscribeUser : user = " + user + "; userSub = " + userSub + "; pwd = " + pwd);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) return Result.error(ErrorCode.BAD_REQUEST);
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		String[] subNameAndDomain = userSub.split("@");
		
		Result<User> subUserResult = validateUserCredentials(subNameAndDomain[1], subNameAndDomain[0], "");
		if (subUserResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		Set<String> subscribedTo = userSubscribedTo.get(user);
		if (subscribedTo != null) subscribedTo.remove(userSub);
		Set<String> subscribers = userSubscribers.get(userSub);
		if (subscribers != null) subscribers.remove(user);
		
		return Result.ok();
	}
	
	@Override
	public Result<List<String>> listSubs(String user) {
		Log.info("listSubs : user = " + user);
		
		String[] nameAndDomain = user.split("@");
		
		if (!nameAndDomain[1].equals(Server.domain)) return Result.error(ErrorCode.NOT_FOUND);
		Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
		if (userResult.error().equals(ErrorCode.NOT_FOUND)) return Result.error(ErrorCode.NOT_FOUND);
		
		return Result.ok(userSubscribedTo.getOrDefault(user, ConcurrentHashMap.newKeySet()).stream().toList());
	}
	
	@Override
	public Result<Void> propagateMessage(Message message, String secret) {
		Log.info("propagateMessage : message = " + message);
		
		if (!secret.equals(Server.secret)) return Result.error(ErrorCode.FORBIDDEN);
		
		Set<String> subscribers = userSubscribers.get(message.getUser() + "@" + message.getDomain());
		
		if (subscribers != null) {
			synchronized (userFeed) {
				for (String subscriber : subscribers) {
					userFeed.putIfAbsent(subscriber, new ConcurrentHashMap<>());
					userFeed.get(subscriber).put(message.getId(), message);
				}
			}
		}
		
		return Result.ok();
	}
	
	@Override
	public Result<Void> deleteUserData(String user, String secret) {
		Log.info("deleteUserData : user = " + user);
		
		if (!secret.equals(Server.secret)) return Result.error(ErrorCode.FORBIDDEN);
		
		userFeed.remove(user);
		userSubscribers.remove(user);
		userSubscribedTo.remove(user);
		
		return Result.ok();
	}
	
	@Override
	public Result<Void> replicateOperation(FeedsOperation operation, String secret) {
		return Result.error(ErrorCode.NOT_IMPLEMENTED);
	}
	
	private Result<User> validateUserCredentials(String domain, String userId, String password) {
		return UsersClientFactory.get(DiscoverySingleton.getInstance().getURI(domain + ":users")).getUser(userId, password);
	}
	
	private void propagateMessageToOtherDomains(Message message) {
		if (ZookeeperReplicationManager.isInitialized() && !ZookeeperReplicationManager.isPrimary()) return;
		for (URI uri : DiscoverySingleton.getInstance().getURIsOfOtherDomainsFeeds(Server.domain)) {
			threadPool.execute(() -> FeedsClientFactory.get(uri).propagateMessage(message, Server.secret));
		}
	}
	
	public List<String> dataStructuresToJson() {
		synchronized (userFeed) {
			synchronized (userSubscribedTo) {
				synchronized (userSubscribers) {
					return List.of(JSON.encode(userFeed), JSON.encode(userSubscribedTo), JSON.encode(userSubscribers));
				}
			}
		}
	}
	
	public void importState(List<String> jsonString) {
		userFeed.putAll(JSON.decode(jsonString.get(0), new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap<Long, Message>>>() {
		}));
		userSubscribedTo.putAll(JSON.decode(jsonString.get(1), new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap.KeySetView<String, Boolean>>>() {
		}));
		userSubscribers.putAll(JSON.decode(jsonString.get(2), new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap.KeySetView<String, Boolean>>>() {
		}));
	}
	
}
