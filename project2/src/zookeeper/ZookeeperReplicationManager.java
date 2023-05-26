package zookeeper;

import api.Message;
import api.java.Feeds;
import api.rest.RestFeeds;
import clients.FeedsClientFactory;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.zookeeper.*;
import servers.Server;
import utils.JSON;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ZookeeperReplicationManager {

	private static final String KAFKA_HOST = "kafka:2181";
	private static final String ROOT = "/" + Server.domain;

	private static final ExecutorService threadPool = Executors.newCachedThreadPool();

	private static ZookeeperLayer zookeeperLayer;
	private static String currentZnodePath, primaryZnodePath, primaryURI = "";
	private static final Map<String, String> secondaryURIs = new ConcurrentHashMap<>();

	private static final AtomicLong versionCounter = new AtomicLong();

	public static void initialize() {
		zookeeperLayer = new ZookeeperLayer(KAFKA_HOST);
		zookeeperLayer.createNode(ROOT, new byte[0], CreateMode.PERSISTENT);
		zookeeperLayer.addWatcher(ROOT, ZookeeperReplicationManager::process);
		currentZnodePath = zookeeperLayer.createNode(ROOT + "/", Server.serverURI.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
		updatePrimary();
	}

	private static void process(WatchedEvent event) {
		System.out.println("Zookeeper Event: " + event.getType() + " " + event.getPath());
		switch (event.getType()) {
			case NodeCreated -> secondaryURIs.put(event.getPath(), zookeeperLayer.getData(event.getPath()));
			case NodeDeleted -> {
				secondaryURIs.remove(event.getPath());
				if (event.getPath().equals(primaryZnodePath)) {
					updatePrimary();
				}
			}
		}
	}

	private static void updatePrimary() {
		List<String> children = zookeeperLayer.getChildren(ROOT);
		primaryZnodePath = ROOT + "/" + children.stream().min(String::compareTo).get();
		secondaryURIs.remove(primaryZnodePath);
		primaryURI = zookeeperLayer.getData(primaryZnodePath);
		System.out.println("New Primary Server Set: " + primaryURI);
	}

	public static void redirectToPrimary(String path) {
		throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI + RestFeeds.PATH + path)).build());
	}

	public static void redirectToPrimary(String path, Object bodyEntity) {
		throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI + RestFeeds.PATH + path)).entity(bodyEntity).build());
	}

	public static void writeToSecondaries(FeedsOperationType type, List<String> args) {
		FeedsOperation operation = new FeedsOperation(versionCounter.getAndIncrement(), type, args);

		CountDownLatch countDownLatch = new CountDownLatch(secondaryURIs.size());

		for (String uri : secondaryURIs.values()) {
			threadPool.submit(() -> {
				FeedsClientFactory.get(URI.create(uri)).replicateOperation(operation, Server.secret);
				countDownLatch.countDown();
			});
		}

		try {
			countDownLatch.await(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean isPrimary() {
		return Server.serverURI.equals(primaryURI);
	}

	public static String primaryURI() {
		return primaryURI;
	}

	public static boolean isInitialized() {
		return zookeeperLayer != null;
	}

	public static long getVersion() {
		return versionCounter.get() - 1;
	}

	public static void updateVersion(FeedsOperation operation) {
		versionCounter.set(Math.max(operation.version(), versionCounter.get()));
	}

}
