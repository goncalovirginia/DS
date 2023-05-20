package mastodon;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.reflect.TypeToken;
import mastodon.msgs.MastodonAccount;
import mastodon.msgs.PostStatusArgs;
import mastodon.msgs.PostStatusResult;
import utils.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.java.Result.ErrorCode.*;
import static api.java.Result.error;
import static api.java.Result.ok;

public class Mastodon implements Feeds {

    static String MASTODON_NOVA_SERVER_URI = "http://10.170.138.52:3000";
    static String MASTODON_SOCIAL_SERVER_URI = "https://mastodon.social";

    static String MASTODON_SERVER_URI = MASTODON_NOVA_SERVER_URI;

    private static final String clientKey = "";
    private static final String clientSecret = "";
    private static final String accessTokenStr = "";

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
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(TIMELINES_PATH));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<PostStatusResult> res = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {});
                return ok(res.stream().map(PostStatusResult::toMessage).toList());
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }


    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        try {
            OAuthRequest request = new OAuthRequest(Verb.DELETE, getEndpoint(STATUSES_PATH)+ "/" + mid);

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok() : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(STATUSES_PATH) + "/" + mid);

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok(JSON.decode(response.getBody(), PostStatusResult.class).toMessage()) : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        try {
            String userSubId = getMastodonUserId(userSub).value();

            OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(String.format(ACCOUNT_FOLLOW_PATH, userSubId)));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok() : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        try {
            String userSubId = getMastodonUserId(userSub).value();

            OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(String.format(ACCOUNT_UNFOLLOW_PATH, userSubId)));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            return response.getCode() == HTTP_OK ? ok() : error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        try {
            String userId = getMastodonUserId(user).value();

            OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(String.format(ACCOUNT_FOLLOWING_PATH, userId)));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<MastodonAccount> accounts = JSON.decode(response.getBody(), new TypeToken<List<MastodonAccount>>() {});
                List<String> accountsNames = accounts.stream().map(MastodonAccount::username).toList();
                return ok(accountsNames);
            }

            return error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Void> propagateMessage(Message message) {
        return error(NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> deleteUserData(String user) {
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
                List<MastodonAccount> res = JSON.decode(response.getBody(), new TypeToken<List<MastodonAccount>>() {});
                for (MastodonAccount account : res) {
                    if (account.id().equals(user)) {
                        userId.put(user, account.id());
                        return ok(account.id());
                    }
                }
            }

            return error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

}
