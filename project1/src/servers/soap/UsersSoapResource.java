package servers.soap;


import api.User;
import api.Users;
import api.service.soap.SoapUsers;
import api.service.soap.UsersException;
import jakarta.jws.WebService;
import servers.resources.UsersResource;

import java.util.List;

@WebService(serviceName = SoapUsers.NAME, targetNamespace = SoapUsers.NAMESPACE, endpointInterface = SoapUsers.INTERFACE)
public class UsersSoapResource implements SoapUsers {
	
	private final Users users;
	
	public UsersSoapResource() {
		users = new UsersResource();
	}
	
	@Override
	public String createUser(User user) throws UsersException {
		return SoapResource.processResult(users.createUser(user), UsersException.class);
	}
	
	@Override
	public User getUser(String userId, String password) throws UsersException {
		return SoapResource.processResult(users.getUser(userId, password), UsersException.class);
	}
	
	@Override
	public User updateUser(String userId, String password, User user) throws UsersException {
		return SoapResource.processResult(users.updateUser(userId, password, user), UsersException.class);
	}
	
	@Override
	public User deleteUser(String userId, String password) throws UsersException {
		return SoapResource.processResult(users.deleteUser(userId, password), UsersException.class);
	}
	
	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		return SoapResource.processResult(users.searchUsers(pattern), UsersException.class);
	}
	
}
