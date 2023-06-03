package servers.rest;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.rest.RestFeeds;
import clients.FeedsClientFactory;
import jakarta.inject.Singleton;
import servers.Server;
import servers.resources.FeedsResource;
import servers.resources.FeedsResourcePreconditions;
import utils.JSON;
import zookeeper.FeedsOperation;
import zookeeper.FeedsOperationType;
import zookeeper.ZookeeperReplicationManager;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class FeedsReplicatedRestResource extends RestResource implements RestFeeds {

	private static final Logger Log = Logger.getLogger(FeedsReplicatedRestResource.class.getName());

	protected Feeds feeds;

	public static final Object operationLock = new Object();
	private static final FeedsResourcePreconditions preconditions = new FeedsResourcePreconditions();

	public FeedsReplicatedRestResource() {
		feeds = new FeedsResource();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s?pwd=%s", user, pwd), msg);
		}

		synchronized (operationLock) {
			fromJavaResult(preconditions.postMessage(user, pwd, msg));
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
			fromJavaResult(preconditions.removeFromPersonalFeed(user, mid, pwd));
			fromJavaResult(feeds.removeFromPersonalFeed(user, mid, pwd));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.removeFromPersonalFeed, List.of(user, String.valueOf(mid), pwd));
		}
	}

	@Override
	public Message getMessage(long version, String user, long mid) {
		if (version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s/%s", user, mid));
		}
		Message r = fromJavaResult(preconditions.getMessage(user, mid));
		if (r != null) return r;
		return fromJavaResult(feeds.getMessage(user, mid));
	}

	@Override
	public List<Message> getMessages(long version, String user, long time) {
		if (version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s?time=%s", user, time));
		}
		List<Message> r = fromJavaResult(preconditions.getMessages(user, time));
		if (r != null) return r;
		return fromJavaResult(feeds.getMessages(user, time));
	}

	@Override
	public void subUser(String user, String userSub, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/%s/%s?pwd=%s", user, userSub, pwd));
		}
		synchronized (operationLock) {
			fromJavaResult(preconditions.subUser(user, userSub, pwd));
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
			fromJavaResult(preconditions.unsubscribeUser(user, userSub, pwd));
			fromJavaResult(feeds.unsubscribeUser(user, userSub, pwd));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.unsubscribeUser, List.of(user, userSub, pwd));
		}
	}

	@Override
	public List<String> listSubs(long version, String user) {
		if (version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/list/%s", user));
		}
		fromJavaResult(preconditions.listSubs(user));
		return fromJavaResult(feeds.listSubs(user));
	}

	@Override
	public void propagateMessage(Message message, String secret) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			new Thread(() -> FeedsClientFactory.get(URI.create(ZookeeperReplicationManager.primaryURI())).propagateMessage(message, secret)).start();
			return;
		}
		fromJavaResult(preconditions.propagateMessage(message, secret));
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
		fromJavaResult(preconditions.deleteUserData(user, secret));
		synchronized (operationLock) {
			fromJavaResult(feeds.deleteUserData(user, secret));
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.deleteUserData, List.of(user, secret));
		}
	}

	@Override
	public void replicateOperation(FeedsOperation operation, String secret) {
		Log.info("replicateOperation : " + operation.type() + " " + operation.version());

		fromJavaResult(preconditions.replicateOperation(operation, secret));
		synchronized (operationLock) {
			if (operation.version() <= ZookeeperReplicationManager.getVersion() && operation.type() != FeedsOperationType.transferState) {
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
