package clients.soap;

import api.User;
import api.java.Result;
import api.java.Users;
import api.soap.SoapUsers;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

public class UsersSoapClient extends SoapClient implements Users {
	
	private SoapUsers stub;
	
	public UsersSoapClient(URI serverURI) {
		super(serverURI);
	}
	
	synchronized private SoapUsers stub() {
		if (stub == null) {
			QName qName = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);
			Service service = Service.create(toURL(serverURI + WSDL), qName);
			this.stub = service.getPort(SoapUsers.class);
			super.setTimeouts((BindingProvider) stub);
		}
		return stub;
	}
	
	@Override
	public Result<String> createUser(User user) {
		return reTry(() -> responseToResult(() -> stub().createUser(user)));
	}
	
	@Override
	public Result<User> getUser(String name, String pwd) {
		return reTry(() -> responseToResult(() -> stub().getUser(name, pwd)));
	}
	
	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		return reTry(() -> responseToResult(() -> stub().updateUser(name, pwd, user)));
	}
	
	@Override
	public Result<User> deleteUser(String name, String pwd) {
		return reTry(() -> responseToResult(() -> stub().deleteUser(name, pwd)));
	}
	
	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return reTry(() -> responseToResult(() -> stub().searchUsers(pattern)));
	}
	
}
