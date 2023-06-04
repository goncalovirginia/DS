package zookeeper;

import api.java.Result;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ZookeeperReplicationManager {

    private static final Logger Log = Logger.getLogger(ZookeeperReplicationManager.class.getName());

    private static final String KAFKA_HOST = "kafka:2181";
    private static final String ROOT = "/" + Server.domain;

    private static final long TIMEOUT_MS = 1000;

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static ZookeeperLayer zookeeperLayer;
    private static String thisZnodePath, primaryZnodePath, primaryURI = "";
    private static final Map<String, String> secondaryURIs = new ConcurrentHashMap<>();
    private static final AtomicLong versionCounter = new AtomicLong();

    private static final BlockingQueue<FeedsOperation> operationQueue = new LinkedBlockingQueue<>();
    private static final Object replicationLock = new Object();

    private static final Thread operationReplicator = new Thread(() -> {
        while (true) {
            try {
                FeedsOperation operation = operationQueue.take();
                replicateToSecondaries(operation);
                synchronized (operation) {
                    operation.notify();
                }
            } catch (InterruptedException ignored) {
            }
        }
    });

    public static void initialize() {
        zookeeperLayer = new ZookeeperLayer(KAFKA_HOST);
        zookeeperLayer.createNode(ROOT, new byte[0], CreateMode.PERSISTENT);
        zookeeperLayer.addWatcher(ROOT, ZookeeperReplicationManager::processZookeeperEvent);
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
            countDownLatch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        updatePrimary();
    }

    private static void processZookeeperEvent(WatchedEvent event) {
        Log.info("Zookeeper Event: " + event.getType() + " " + event.getPath());
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
        if (isPrimary()) {
            secondaryURIs.remove(thisZnodePath);
            operationReplicator.start();
        }
        Log.info("Primary Server Updated: " + primaryURI);
    }

    public static void redirectToPrimary(String path) {
        throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI + RestFeeds.PATH + path)).build());
    }

    public static void redirectToPrimary(String path, Object bodyEntity) {
        throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI + RestFeeds.PATH + path)).entity(bodyEntity).build());
    }

    public static void enqueueOperationAndAwaitReplication(FeedsOperationType type, List<String> args) {
        FeedsOperation operation;

        synchronized (versionCounter) {
            operation = new FeedsOperation(versionCounter.incrementAndGet(), type, args);
            operationQueue.add(operation);
        }

        synchronized (operation) {
            try {
                operation.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static void replicateToSecondaries(FeedsOperation operation) {
        synchronized (replicationLock) {
            Log.info("writeToSecondaries : " + operation.type() + " " + operation.version());

            CountDownLatch countDownLatch = new CountDownLatch(1);

            for (String uri : secondaryURIs.values()) {
                threadPool.execute(() -> {
                    FeedsClientFactory.get(URI.create(uri)).replicateOperation(operation, Server.secret);
                    countDownLatch.countDown();
                });
            }
            try {
                countDownLatch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static void transferStateToSecondary(String secondaryURI) {
        synchronized (replicationLock) {
            List<String> args = FeedsReplicatedRestServer.resourceInstance.getState();
            FeedsOperation operation = new FeedsOperation(versionCounter.get(), FeedsOperationType.transferState, args);

            Log.info("transferStateToSecondary : " + operation.type() + " " + operation.version());

            Future<Result<Void>> future = threadPool.submit(() -> FeedsClientFactory.get(URI.create(secondaryURI)).replicateOperation(operation, Server.secret));
            try {
                future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {
            }
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
