package servers.resources;

import api.Message;
import api.User;
import api.java.Feeds;
import api.java.Result;
import clients.FeedsClientFactory;
import clients.UsersClientFactory;
import discovery.DiscoverySingleton;
import servers.Server;
import zookeeper.FeedsOperation;
import zookeeper.FeedsOperationType;
import zookeeper.ZookeeperReplicationManager;

import java.util.List;

public class FeedsResourcePreconditions implements Feeds {

    public FeedsResourcePreconditions() {

    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        if (msg.getUser() == null || !msg.getUser().equals(nameAndDomain[0]) || msg.getDomain() == null || !msg.getDomain().equals(Server.domain)) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        return Result.ok();
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) {
            return FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(nameAndDomain[1] + ":feeds")).getMessage(user, mid);
        }

        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
        if (userResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) {
            return FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(nameAndDomain[1] + ":feeds")).getMessages(user, time);
        }

        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
        if (userResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        String[] subNameAndDomain = userSub.split("@");

        Result<User> subUserResult = validateUserCredentials(subNameAndDomain[1], subNameAndDomain[0], "");
        if (subUserResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        String[] subNameAndDomain = userSub.split("@");

        Result<User> subUserResult = validateUserCredentials(subNameAndDomain[1], subNameAndDomain[0], "");
        if (subUserResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.NOT_FOUND);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
        if (userResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    @Override
    public Result<Void> propagateMessage(Message message, String secret) {
        return secret.equals(Server.secret) ? Result.ok() : Result.error(Result.ErrorCode.FORBIDDEN);
    }

    @Override
    public Result<Void> deleteUserData(String user, String secret) {
        return secret.equals(Server.secret) ? Result.ok() : Result.error(Result.ErrorCode.FORBIDDEN);
    }

    @Override
    public Result<Void> replicateOperation(FeedsOperation operation, String secret) {
        if (!secret.equals(Server.secret)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
        if (operation.version() <= ZookeeperReplicationManager.getVersion() && operation.type() != FeedsOperationType.transferState) {
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        return Result.ok();
    }

    private Result<User> validateUserCredentials(String domain, String userId, String password) {
        return UsersClientFactory.get(DiscoverySingleton.getInstance().getURI(domain + ":users")).getUser(userId, password);
    }

}
