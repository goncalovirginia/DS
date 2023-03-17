package clients.soap;

import api.Result;
import api.User;
import api.Users;
import clients.soap.SoapClient;
import jakarta.xml.ws.Service;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class UsersSoapClient extends SoapClient implements Users {
	
	private final SoapUsers users;
	
	public UsersSoapClient(URI serverURI) throws IOException {
		URL url = new URL(serverURI + "?wsdl");
		URLConnection urlConnection = url.openConnection();
		urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
		urlConnection.setReadTimeout(READ_TIMEOUT);
		urlConnection.connect();
		
		QName qname = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);
		Service service = Service.create(url, qname);
		users = service.getPort(SoapUsers.class);
	}
	
	@Override
	public Result<String> createUser(User user) {
		return reTry(() -> responseToResult(() -> users.createUser(user)));
	}
	
	@Override
	public Result<User> getUser(String userId, String password) {
		return reTry(() -> responseToResult(() -> users.getUser(userId, password)));
	}
	
	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return reTry(() -> responseToResult(() -> users.updateUser(userId, password, user)));
	}
	
	@Override
	public Result<User> deleteUser(String userId, String password) {
		return reTry(() -> responseToResult(() -> users.deleteUser(userId, password)));
	}
	
	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return reTry(() -> responseToResult(() -> users.searchUsers(pattern)));
	}
	
}
