package servers.soap;


import api.User;
import api.java.Users;
import api.soap.UsersException;
import api.soap.UsersService;
import jakarta.jws.WebService;
import servers.resources.UsersResource;

import java.util.List;
import java.util.logging.Logger;

@WebService(serviceName = UsersService.NAME, targetNamespace = UsersService.NAMESPACE, endpointInterface = UsersService.INTERFACE)
public class UsersSoapResource extends SoapResource<UsersException> implements UsersService {
	
	private static final Logger Log = Logger.getLogger(UsersSoapResource.class.getName());
	
	private final Users users;
	
	public UsersSoapResource() {
		super((result) -> new UsersException(result.error().toString()));
		this.users = new UsersResource();
	}
	
	@Override
	public String createUser(User user) throws UsersException {
		return fromJavaResult(users.createUser(user));
	}
	
	@Override
	public User getUser(String name, String pwd) throws UsersException {
		return fromJavaResult(users.getUser(name, pwd));
	}
	
	@Override
	public User updateUser(String name, String pwd, User user) throws UsersException {
		return fromJavaResult(users.updateUser(name, pwd, user));
	}
	
	@Override
	public User deleteUser(String name, String pwd) throws UsersException {
		return fromJavaResult(users.deleteUser(name, pwd));
	}
	
	@Override
	public List<User> searchUsers(String pattern) throws UsersException {
		return fromJavaResult(users.searchUsers(pattern));
	}
	
}
