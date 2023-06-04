package servers.resources;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.java.Result.ErrorCode;
import clients.FeedsClientFactory;
import com.google.gson.reflect.TypeToken;
import discovery.DiscoverySingleton;
import servers.Server;
import utils.JSON;
import zookeeper.FeedsOperation;
import zookeeper.ZookeeperReplicationManager;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        Map<Long, Message> feed = userFeed.get(user);
        if (feed == null || feed.remove(mid) == null) return Result.error(ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info("getMessage : user = " + user + "; mid = " + mid);

        Map<Long, Message> feed = userFeed.get(user);
        Message message;
        if (feed == null || (message = feed.get(mid)) == null) return Result.error(ErrorCode.NOT_FOUND);

        return Result.ok(message);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Log.info("getMessages : user = " + user + "; time = " + time);

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

        Set<String> subscribedTo = userSubscribedTo.get(user);
        if (subscribedTo == null || !subscribedTo.remove(userSub)) return Result.error(ErrorCode.NOT_FOUND);
        Set<String> subscribers = userSubscribers.get(userSub);
        if (subscribers != null) subscribers.remove(user);

        return Result.ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info("listSubs : user = " + user);

        return Result.ok(userSubscribedTo.getOrDefault(user, ConcurrentHashMap.newKeySet()).stream().toList());
    }

    @Override
    public Result<Void> propagateMessage(Message message, String secret) {
        Log.info("propagateMessage : message = " + message);

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

        userFeed.remove(user);
        userSubscribers.remove(user);
        userSubscribedTo.remove(user);

        return Result.ok();
    }

    @Override
    public Result<Void> replicateOperation(FeedsOperation operation, String secret) {
        return Result.error(ErrorCode.NOT_IMPLEMENTED);
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
        Log.info("importState");
        userFeed.clear();
        userFeed.putAll(JSON.decode(jsonString.get(0), new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap<Long, Message>>>() {
        }));
        userSubscribedTo.clear();
        userSubscribedTo.putAll(JSON.decode(jsonString.get(1), new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap.KeySetView<String, Boolean>>>() {
        }));
        userSubscribers.clear();
        userSubscribers.putAll(JSON.decode(jsonString.get(2), new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap.KeySetView<String, Boolean>>>() {
        }));
    }

}
