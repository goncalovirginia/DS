package zookeeper;

import api.rest.RestFeeds;
import clients.FeedsClientFactory;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import servers.Server;
import servers.rest.FeedsReplicatedRestServer;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ZookeeperReplicationManager {
	
	private static final String KAFKA_HOST = "kafka:2181";
	private static final String ROOT = "/" + Server.domain;
	
	private static final long COUNTDOWNLATCH_TIMEOUT_MS = 1000;
	
	private static final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	private static ZookeeperLayer zookeeperLayer;
	private static String thisZnodePath, primaryZnodePath, primaryURI = "";
	private static final Map<String, String> secondaryURIs = new ConcurrentHashMap<>();
	
	private static final AtomicLong versionCounter = new AtomicLong();
	
	private static final BlockingQueue<FeedsOperation> operationQueue = new LinkedBlockingQueue<>();
	
	private static final Thread operationQueueConsumer = new Thread(() -> {
		while (true) {
			try {
				FeedsOperation operation = operationQueue.take();
				writeToSecondaries(operation);
				operation.notify();
			} catch (InterruptedException ignored) {
			}
		}
	});
	
	public static void initialize() {
		zookeeperLayer = new ZookeeperLayer(KAFKA_HOST);
		zookeeperLayer.createNode(ROOT, new byte[0], CreateMode.PERSISTENT);
		zookeeperLayer.addWatcher(ROOT, ZookeeperReplicationManager::process);
		thisZnodePath = zookeeperLayer.createNode(ROOT + "/", Server.serverURI.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
		
		List<String> children = zookeeperLayer.getChildren(ROOT);
		CountDownLatch countDownLatch = new CountDownLatch(children.size());
		
		for (String child : children) {
			threadPool.execute(() -> {
				String childPath = ROOT + "/" + child;
				secondaryURIs.put(childPath, zookeeperLayer.getData(childPath));
				countDownLatch.countDown();
			});
		}
		try {
			countDownLatch.await(COUNTDOWNLATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignored) {
		}
		
		updatePrimary();
		operationQueueConsumer.start();
	}
	
	private static void process(WatchedEvent event) {
		System.out.println("Zookeeper Event: " + event.getType() + " " + event.getPath());
		switch (event.getType()) {
			case NodeCreated -> {
				secondaryURIs.put(event.getPath(), zookeeperLayer.getData(event.getPath()));
				if (isPrimary()) transferStateToSecondary(secondaryURIs.get(event.getPath()));
			}
			case NodeDeleted -> {
				secondaryURIs.remove(event.getPath());
				if (event.getPath().equals(primaryZnodePath)) updatePrimary();
			}
		}
	}
	
	private static void updatePrimary() {
		primaryZnodePath = secondaryURIs.keySet().stream().min(String::compareTo).get();
		primaryURI = secondaryURIs.get(primaryZnodePath);
		if (isPrimary()) secondaryURIs.remove(thisZnodePath);
		System.out.println("Primary Server Updated: " + primaryURI);
	}
	
	public static void redirectToPrimary(String path) {
		throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI + RestFeeds.PATH + path)).build());
	}
	
	public static void redirectToPrimary(String path, Object bodyEntity) {
		throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI + RestFeeds.PATH + path)).entity(bodyEntity).build());
	}
	
	public static void writeToSecondaries(FeedsOperation operation) {
		CountDownLatch countDownLatch = new CountDownLatch(secondaryURIs.size());
		
		for (String uri : secondaryURIs.values()) {
			threadPool.execute(() -> {
				FeedsClientFactory.get(URI.create(uri)).replicateOperation(operation, Server.secret);
				countDownLatch.countDown();
			});
		}
		try {
			countDownLatch.await(COUNTDOWNLATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignored) {
		}
	}
	
	public static void queueOperation(FeedsOperationType type, List<String> args) {
		try {
			FeedsOperation operation = new FeedsOperation(versionCounter.incrementAndGet(), type, args);
			operationQueue.put(operation);
			operation.wait();
		} catch (InterruptedException ignored){
		}
	}
	
	private static void transferStateToSecondary(String secondaryURI) {
		List<String> feedsResourceInstanceDataStructuresJson = FeedsReplicatedRestServer.feedsReplicatedRestResourceInstance.getResourceInstanceDataStructuresJSONs();
		FeedsOperation operation = new FeedsOperation(versionCounter.get(), FeedsOperationType.transferState, feedsResourceInstanceDataStructuresJson);
		CountDownLatch countDownLatch = new CountDownLatch(1);
		
		threadPool.execute(() -> {
			FeedsClientFactory.get(URI.create(secondaryURI)).replicateOperation(operation, Server.secret);
			countDownLatch.countDown();
		});
		try {
			countDownLatch.await(COUNTDOWNLATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignored) {
		}
	}
	
	public static boolean isPrimary() {
		return thisZnodePath.equals(primaryZnodePath);
	}
	
	public static String primaryURI() {
		return primaryURI;
	}
	
	public static boolean isInitialized() {
		return zookeeperLayer != null;
	}
	
	public static long getVersion() {
		return versionCounter.get();
	}
	
	public static void setVersion(long version) {
		versionCounter.set(version);
	}
	
}
