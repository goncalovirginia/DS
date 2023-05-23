package servers.soap;

import api.Message;
import api.java.Feeds;
import api.soap.FeedsException;
import api.soap.FeedsService;
import jakarta.jws.WebService;
import servers.resources.FeedsResource;

import java.util.List;

@WebService(serviceName = FeedsService.NAME, targetNamespace = FeedsService.NAMESPACE, endpointInterface = FeedsService.INTERFACE)
public class FeedsSoapResource extends SoapResource<FeedsException> implements FeedsService {

	private final Feeds feeds;

	public FeedsSoapResource() {
		super((result) -> new FeedsException(result.error().toString()));
		this.feeds = new FeedsResource();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) throws FeedsException {
		return fromJavaResult(feeds.postMessage(user, pwd, msg));
	}

	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) throws FeedsException {
		fromJavaResult(feeds.removeFromPersonalFeed(user, mid, pwd));
	}

	@Override
	public Message getMessage(String user, long mid) throws FeedsException {
		return fromJavaResult(feeds.getMessage(user, mid));
	}

	@Override
	public List<Message> getMessages(String user, long time) throws FeedsException {
		return fromJavaResult(feeds.getMessages(user, time));
	}

	@Override
	public void subUser(String user, String userSub, String pwd) throws FeedsException {
		fromJavaResult(feeds.subUser(user, userSub, pwd));
	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) throws FeedsException {
		fromJavaResult(feeds.unsubscribeUser(user, userSub, pwd));
	}

	@Override
	public List<String> listSubs(String user) throws FeedsException {
		return fromJavaResult(feeds.listSubs(user));
	}

	@Override
	public void propagateMessage(Message message) throws FeedsException {
		fromJavaResult(feeds.propagateMessage(message));
	}

	@Override
	public void deleteUserData(String user) throws FeedsException {
		fromJavaResult(feeds.deleteUserData(user));
	}

}
