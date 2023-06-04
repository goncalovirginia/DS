package mastodon;

import api.Message;
import api.User;
import api.java.Feeds;
import api.java.Result;
import clients.FeedsClientFactory;
import clients.UsersClientFactory;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.reflect.TypeToken;
import discovery.DiscoverySingleton;
import mastodon.msgs.MastodonAccount;
import mastodon.msgs.PostStatusArgs;
import mastodon.msgs.PostStatusResult;
import servers.Server;
import utils.JSON;
import zookeeper.FeedsOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static api.java.Result.ErrorCode.*;
import static api.java.Result.error;
import static api.java.Result.ok;

public class Mastodon implements Feeds {

    private static final Logger Log = Logger.getLogger(Mastodon.class.getName());

    static String MASTODON_NOVA_SERVER_URI = "http://10.170.138.52:3000";
    static String MASTODON_SOCIAL_SERVER_URI = "https://mastodon.social";

    static String MASTODON_SERVER_URI = MASTODON_NOVA_SERVER_URI;

    private static final String clientKey = "piAPZ6huUnIP_Wd3qXofhZ6uHUQlB_xnsU03FhP8QQY";
    private static final String clientSecret = "rRsfXIk3Hr7VJ_wJJ6SGVa9KEG2e-xKbPnBOUlHEqH8";
    private static final String accessTokenStr = "GXOcVyV_KsvWWISspqA83PMr9TgIp4bqHuXsAwv51es";

    static final String STATUSES_PATH = "/api/v1/statuses";
    static final String TIMELINES_PATH = "/api/v1/timelines/home";
    static final String ACCOUNT_FOLLOWING_PATH = "/api/v1/accounts/%s/following";
    static final String VERIFY_CREDENTIALS_PATH = "/api/v1/accounts/verify_credentials";
    static final String SEARCH_ACCOUNTS_PATH = "/api/v1/accounts/search";
    static final String ACCOUNT_FOLLOW_PATH = "/api/v1/accounts/%s/follow";
    static final String ACCOUNT_UNFOLLOW_PATH = "/api/v1/accounts/%s/unfollow";

    private static final int HTTP_OK = 200;

    private final Map<String, String> userId;

    protected OAuth20Service service;
    protected OAuth2AccessToken accessToken;

    private static Mastodon impl;

    public Mastodon() {
        userId = new HashMap<>();
        try {
            service = new ServiceBuilder(clientKey).apiSecret(clientSecret).build(MastodonApi.instance());
            accessToken = new OAuth2AccessToken(accessTokenStr);
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(0);
        }
    }

    synchronized public static Mastodon getInstance() {
        if (impl == null)
            impl = new Mastodon();
        return impl;
    }

    private String getEndpoint(String path, Object... args) {
        var fmt = MASTODON_SERVER_URI + path;
        return String.format(fmt, args);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Log.info("postMessage : user = " + user + "; pwd = " + pwd + "; msg = " + msg);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        if (msg.getUser() == null || !msg.getUser().equals(nameAndDomain[0]) || msg.getDomain() == null || !msg.getDomain().equals(Server.domain)) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        try {
            OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(STATUSES_PATH));

            JSON.toMap(new PostStatusArgs(msg.getText())).forEach((k, v) -> {
                request.addBodyParameter(k, v.toString());
            });

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok(JSON.decode(response.getBody(), PostStatusResult.class).getId()) : error(BAD_REQUEST);

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_SERVER_ERROR);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Log.info("getMessages : user = " + user + "; time = " + time);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) {
            return FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(nameAndDomain[1] + ":feeds")).getMessages(user, time);
        }

        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
        if (userResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(TIMELINES_PATH));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<PostStatusResult> res = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {
                });
                return ok(res.stream().map(PostStatusResult::toMessage).filter((p) -> p.getCreationTime() > time).toList());
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }


    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info("removeFromPersonalFeed : user = " + user + "; mid = " + mid + "; pwd = " + pwd);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        try {
            OAuthRequest request = new OAuthRequest(Verb.DELETE, getEndpoint(STATUSES_PATH) + "/" + mid);

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok() : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info("getMessage : user = " + user + "; mid = " + mid);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) {
            return FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(nameAndDomain[1] + ":feeds")).getMessage(user, mid);
        }

        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
        if (userResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(STATUSES_PATH) + "/" + mid);

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok(JSON.decode(response.getBody(), PostStatusResult.class).toMessage()) : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Log.info("subUser : user = " + user + "; userSub = " + userSub + "; pwd = " + pwd);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        try {
            String userSubId = getMastodonUserId(userSub).value();

            OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(String.format(ACCOUNT_FOLLOW_PATH, userSubId)));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok() : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        Log.info("unsubscribeUser : user = " + user + "; userSub = " + userSub + "; pwd = " + pwd);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.BAD_REQUEST);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], pwd);
        if (!userResult.isOK()) return Result.error(userResult.error());

        try {
            String userSubId = getMastodonUserId(userSub).value();

            OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(String.format(ACCOUNT_UNFOLLOW_PATH, userSubId)));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok() : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info("listSubs : user = " + user);

        String[] nameAndDomain = user.split("@");

        if (!nameAndDomain[1].equals(Server.domain)) return Result.error(Result.ErrorCode.NOT_FOUND);
        Result<User> userResult = validateUserCredentials(Server.domain, nameAndDomain[0], "");
        if (userResult.error().equals(Result.ErrorCode.NOT_FOUND)) return Result.error(Result.ErrorCode.NOT_FOUND);

        try {
            String userId = getMastodonUserId(user).value();

            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(String.format(ACCOUNT_FOLLOWING_PATH, userId)));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<MastodonAccount> accounts = JSON.decode(response.getBody(), new TypeToken<List<MastodonAccount>>() {
                });
                List<String> accountsNames = accounts.stream().map(MastodonAccount::username).toList();
                return ok(accountsNames);
            }

            return error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Result<Void> propagateMessage(Message message, String secret) {
        return error(NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> deleteUserData(String user, String secret) {
        return error(NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> replicateOperation(FeedsOperation operation, String secret) {
        return error(NOT_IMPLEMENTED);
    }

    private Result<String> getMastodonUserId(String user) {
        if (userId.containsKey(user)) {
            return ok(userId.get(user));
        }

        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(SEARCH_ACCOUNTS_PATH));
            request.addQuerystringParameter("q", user);
            request.addQuerystringParameter("limit", "10");

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                String username = user.split("@")[0];
                List<MastodonAccount> res = JSON.decode(response.getBody(), new TypeToken<List<MastodonAccount>>() {
                });
                for (MastodonAccount account : res) {
                    if (account.username().equals(username)) {
                        userId.put(user, account.id());
                        return ok(account.id());
                    }
                }
            }

            return error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private Result<User> validateUserCredentials(String domain, String userId, String password) {
        return UsersClientFactory.get(DiscoverySingleton.getInstance().getURI(domain + ":users")).getUser(userId, password);
    }

}
