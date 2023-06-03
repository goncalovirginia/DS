package zookeeper;

import org.apache.zookeeper.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZookeeperLayer {

	private static ZooKeeper client;
	private final int TIMEOUT = 5000;

	public ZookeeperLayer(String host) {
		connect(host);
	}

	public synchronized ZooKeeper client() {
		if (client == null || !client.getState().equals(ZooKeeper.States.CONNECTED)) {
			throw new IllegalStateException("ZooKeeper is not connected.");
		}
		return client;
	}

	private void connect(String host) {
		try {
			CountDownLatch connectedSignal = new CountDownLatch(1);
			client = new ZooKeeper(host, TIMEOUT, (e) -> {
				if (e.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
					connectedSignal.countDown();
				}
			});
			connectedSignal.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String createNode(String path, byte[] data, CreateMode mode) {
		try {
			return client().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
		} catch (KeeperException.NodeExistsException x) {
			return path;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	public List<String> getChildren(String path) {
		try {
			return client().getChildren(path, false);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}

	public List<String> getChildren(String path, Watcher watcher) {
		try {
			return client().getChildren(path, watcher);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}

	public String getData(String path) {
		try {
			return new String(client().getData(path, false, null));
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addWatcher(String path, Watcher watcher) {
		try {
			client().addWatch(path, watcher, AddWatchMode.PERSISTENT_RECURSIVE);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		String host = args.length == 0 ? "localhost" : args[0];

		var zookeeper = new ZookeeperLayer(host);

		String root = "/feeds2";

		String path = zookeeper.createNode(root, new byte[0], CreateMode.PERSISTENT);
		System.out.println("PERSISTENT: " + path);

		zookeeper.getChildren(root).forEach(System.out::println);

		for (int i = 0; i < 10; i++) {
			String childPath = zookeeper.createNode(root + "/", ("uri" + i).getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("EPHEMERAL: " + childPath);
		}

		System.out.println("/feeds2 CHILDREN:");
		zookeeper.getChildren(root).forEach((c) -> {
			System.out.println(c);
			try {
				System.out.println(new String(client.getData("/feeds2/" + c, false, null)));
			} catch (KeeperException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
