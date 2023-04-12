package clients.soap;

import api.Message;
import api.java.Feeds;
import api.java.Result;
import api.soap.SoapFeeds;
import api.soap.SoapUsers;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

public class FeedsSoapClient extends SoapClient implements Feeds {
	
	private SoapFeeds stub;
	
	public FeedsSoapClient(URI serverURI) {
		super(serverURI);
	}
	
	synchronized private SoapFeeds stub() {
		if (stub == null) {
			QName qName = new QName(SoapFeeds.NAMESPACE, SoapFeeds.NAME);
			Service service = Service.create(toURL(serverURI + WSDL), qName);
			this.stub = service.getPort(SoapFeeds.class);
			super.setTimeouts((BindingProvider) stub);
		}
		return stub;
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		return null;
	}
	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		return null;
	}
	
	@Override
	public Result<Message> getMessage(String user, long mid) {
		return null;
	}
	
	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		return null;
	}
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		return null;
	}
	
	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		return null;
	}
	
	@Override
	public Result<List<String>> listSubs(String user) {
		return null;
	}
	
	@Override
	public Result<Void> propagateMessage(Message message) {
		return reTry(() -> responseToResult(() -> stub().propagateMessage(message)));
	}
	
}
