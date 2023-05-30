package servers.rest;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.rest.RestFeeds;
import clients.FeedsClientFactory;
import jakarta.inject.Singleton;
import servers.Server;
import servers.resources.FeedsResource;
import utils.JSON;
import zookeeper.FeedsOperation;
import zookeeper.FeedsOperationType;
import zookeeper.ZookeeperReplicationManager;

import java.net.URI;
import java.util.List;

@Singleton
public class FeedsReplicatedRestResource extends RestResource implements RestFeeds {
	
	protected Feeds feeds;
	
	public static final Object operationLock = new Object();
	
	public FeedsReplicatedRestResource() {
		feeds = new FeedsResource();
	}
	
	@Override
	public long postMessage(String user, String pwd, Message msg) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s?pwd=%s", user, pwd), msg);
		}
		synchronized (operationLock) {
			long id = fromJavaResult(feeds.postMessage(user, pwd, msg));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.postMessage, List.of(user, pwd, JSON.encode(msg)));
			return id;
		}
	}
	
	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s/%s?pwd=%s", user, mid, pwd));
		}
		synchronized (operationLock) {
			fromJavaResult(feeds.removeFromPersonalFeed(user, mid, pwd));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.removeFromPersonalFeed, List.of(user, String.valueOf(mid), pwd));
		}
	}
	
	@Override
	public Message getMessage(long version, String user, long mid) {
		if (version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s/%s", user, mid));
		}
		return fromJavaResult(feeds.getMessage(user, mid));
	}
	
	@Override
	public List<Message> getMessages(long version, String user, long time) {
		if (version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s?time=%s", user, time));
		}
		return fromJavaResult(feeds.getMessages(user, time));
	}
	
	@Override
	public void subUser(String user, String userSub, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/%s/%s?pwd=%s", user, userSub, pwd));
		}
		synchronized (operationLock) {
			fromJavaResult(feeds.subUser(user, userSub, pwd));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.subUser, List.of(user, userSub, pwd));
		}
	}
	
	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/%s/%s?pwd=%s", user, userSub, pwd));
		}
		synchronized (operationLock) {
			fromJavaResult(feeds.unsubscribeUser(user, userSub, pwd));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.unsubscribeUser, List.of(user, userSub, pwd));
		}
	}
	
	@Override
	public List<String> listSubs(long version, String user) {
		if (version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/list/%s", user));
		}
		return fromJavaResult(feeds.listSubs(user));
	}
	
	@Override
	public void propagateMessage(Message message, String secret) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			new Thread(() -> FeedsClientFactory.get(URI.create(ZookeeperReplicationManager.primaryURI())).propagateMessage(message, secret)).start();
			return;
		}
		synchronized (operationLock) {
			fromJavaResult(feeds.propagateMessage(message, secret));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.propagateMessage, List.of(JSON.encode(message), secret));
		}
	}
	
	@Override
	public void deleteUserData(String user, String secret) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			new Thread(() -> FeedsClientFactory.get(URI.create(ZookeeperReplicationManager.primaryURI())).deleteUserData(user, secret)).start();
			return;
		}
		synchronized (operationLock) {
			fromJavaResult(feeds.deleteUserData(user, secret));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.deleteUserData, List.of(user, secret));
		}
	}
	
	@Override
	public void replicateOperation(FeedsOperation operation, String secret) {
		if (!secret.equals(Server.secret)) {
			fromJavaResult(Result.error(Result.ErrorCode.FORBIDDEN));
		}
		synchronized (operationLock) {
			if (operation.version() <= ZookeeperReplicationManager.getVersion()) {
				fromJavaResult(Result.error(Result.ErrorCode.CONFLICT));
			}
			executeOperation(operation);
		}
	}
	
	private void executeOperation(FeedsOperation operation) {
		List<String> args = operation.args();
		switch (operation.type()) {
			case postMessage -> feeds.postMessage(args.get(0), args.get(1), JSON.decode(args.get(2), Message.class));
			case removeFromPersonalFeed -> feeds.removeFromPersonalFeed(args.get(0), Long.parseLong(args.get(1)), args.get(2));
			case subUser -> feeds.subUser(args.get(0), args.get(1), args.get(2));
			case unsubscribeUser -> feeds.unsubscribeUser(args.get(0), args.get(1), args.get(2));
			case propagateMessage -> feeds.propagateMessage(JSON.decode(args.get(0), Message.class), args.get(1));
			case deleteUserData -> feeds.deleteUserData(args.get(0), args.get(1));
			case transferState -> ((FeedsResource) feeds).importState(operation.args());
		}
		ZookeeperReplicationManager.setVersion(operation.version());
	}
	
	public List<String> getState() {
		return ((FeedsResource) feeds).dataStructuresToJson();
	}
	
}
