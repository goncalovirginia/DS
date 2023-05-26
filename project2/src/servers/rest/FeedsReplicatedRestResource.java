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

	public FeedsReplicatedRestResource() {
		feeds = new FeedsResource();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s?pwd=%s", user, pwd), msg);
		}
		Result<Long> result = feeds.postMessage(user, pwd, msg);
		if (result.isOK()) {
			ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.postMessage, List.of(user, pwd, JSON.encode(msg)));
		}
		return fromJavaResult(result);
	}

	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s/%s?pwd=%s", user, mid, pwd));
		}
		fromJavaResult(feeds.removeFromPersonalFeed(user, mid, pwd));
		ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.removeFromPersonalFeed, List.of(user, String.valueOf(mid), pwd));
	}

	@Override
	public Message getMessage(long version, String user, long mid) {
		if (!ZookeeperReplicationManager.isPrimary() || version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s/%s", user, mid));
		}
		return fromJavaResult(feeds.getMessage(user, mid));
	}

	@Override
	public List<Message> getMessages(long version, String user, long time) {
		if (!ZookeeperReplicationManager.isPrimary() || version > ZookeeperReplicationManager.getVersion()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/%s", user));
		}
		return fromJavaResult(feeds.getMessages(user, time));
	}

	@Override
	public void subUser(String user, String userSub, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/%s/%s?pwd=%s", user, userSub, pwd));
		}
		fromJavaResult(feeds.subUser(user, userSub, pwd));
		ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.subUser, List.of(user, userSub, pwd));
	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			ZookeeperReplicationManager.redirectToPrimary(String.format("/sub/%s/%s?pwd=%s", user, userSub, pwd));
		}
		fromJavaResult(feeds.unsubscribeUser(user, userSub, pwd));
		ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.unsubscribeUser, List.of(user, userSub, pwd));
	}

	@Override
	public List<String> listSubs(long version, String user) {
		if (!ZookeeperReplicationManager.isPrimary() || version > ZookeeperReplicationManager.getVersion()) {
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
		fromJavaResult(feeds.propagateMessage(message, secret));
		ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.propagateMessage, List.of(JSON.encode(message), secret));
	}

	@Override
	public void deleteUserData(String user, String secret) {
		if (!ZookeeperReplicationManager.isPrimary()) {
			new Thread(() -> FeedsClientFactory.get(URI.create(ZookeeperReplicationManager.primaryURI())).deleteUserData(user, secret)).start();
			return;
		}
		fromJavaResult(feeds.deleteUserData(user, secret));
		ZookeeperReplicationManager.writeToSecondaries(FeedsOperationType.deleteUserData, List.of(user, secret));
	}

	@Override
	public void replicateOperation(FeedsOperation operation, String secret) {
		if (!secret.equals(Server.secret)) {
			fromJavaResult(Result.error(Result.ErrorCode.FORBIDDEN));
		}
		operationSwitch(operation);
	}

	private void operationSwitch(FeedsOperation operation) {
		List<String> args = operation.args();
		switch (operation.type()) {

			case postMessage -> feeds.postMessage(args.get(0), args.get(1), JSON.decode(args.get(2), Message.class));
			case removeFromPersonalFeed -> feeds.removeFromPersonalFeed(args.get(0), Long.parseLong(args.get(1)), args.get(2));
			case subUser -> feeds.subUser(args.get(0), args.get(1), args.get(2));
			case unsubscribeUser -> feeds.unsubscribeUser(args.get(0), args.get(1), args.get(2));
			case propagateMessage -> feeds.propagateMessage(JSON.decode(args.get(0), Message.class), args.get(1));
			case deleteUserData -> feeds.deleteUserData(args.get(0), args.get(1));
		}
		ZookeeperReplicationManager.updateVersion(operation);
	}

}
