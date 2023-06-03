package servers.soap;

import api.Message;
import api.java.Feeds;
import api.soap.FeedsException;
import api.soap.FeedsService;
import jakarta.jws.WebService;
import servers.resources.FeedsResource;
import servers.resources.FeedsResourcePreconditions;

import java.util.List;

@WebService(serviceName = FeedsService.NAME, targetNamespace = FeedsService.NAMESPACE, endpointInterface = FeedsService.INTERFACE)
public class FeedsSoapResource extends SoapResource<FeedsException> implements FeedsService {

	private final Feeds feeds;

	private static final FeedsResourcePreconditions preconditions = new FeedsResourcePreconditions();

	public FeedsSoapResource() {
		super((result) -> new FeedsException(result.error().toString()));
		this.feeds = new FeedsResource();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) throws FeedsException {
		fromJavaResult(preconditions.postMessage(user, pwd, msg));
		return fromJavaResult(feeds.postMessage(user, pwd, msg));
	}

	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) throws FeedsException {
		fromJavaResult(preconditions.removeFromPersonalFeed(user, mid, pwd));
		fromJavaResult(feeds.removeFromPersonalFeed(user, mid, pwd));
	}

	@Override
	public Message getMessage(String user, long mid) throws FeedsException {
		Message r = fromJavaResult(preconditions.getMessage(user, mid));
		if (r != null) return r;
		return fromJavaResult(feeds.getMessage(user, mid));
	}

	@Override
	public List<Message> getMessages(String user, long time) throws FeedsException {
		List<Message> r = fromJavaResult(preconditions.getMessages(user, time));
		if (r != null) return r;
		return fromJavaResult(feeds.getMessages(user, time));
	}

	@Override
	public void subUser(String user, String userSub, String pwd) throws FeedsException {
		fromJavaResult(preconditions.subUser(user, userSub, pwd));
		fromJavaResult(feeds.subUser(user, userSub, pwd));
	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) throws FeedsException {
		fromJavaResult(preconditions.unsubscribeUser(user, userSub, pwd));
		fromJavaResult(feeds.unsubscribeUser(user, userSub, pwd));
	}

	@Override
	public List<String> listSubs(String user) throws FeedsException {
		fromJavaResult(preconditions.listSubs(user));
		return fromJavaResult(feeds.listSubs(user));
	}

	@Override
	public void propagateMessage(Message message, String secret) throws FeedsException {
		fromJavaResult(preconditions.propagateMessage(message, secret));
		fromJavaResult(feeds.propagateMessage(message, secret));
	}

	@Override
	public void deleteUserData(String user, String secret) throws FeedsException {
		fromJavaResult(preconditions.deleteUserData(user, secret));
		fromJavaResult(feeds.deleteUserData(user, secret));
	}

}
