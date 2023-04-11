package servers.rest;

import api.User;
import api.java.Users;
import api.rest.RestUsers;
import jakarta.inject.Singleton;
import servers.resources.UsersResource;

import java.util.List;

@Singleton
public class UsersRestResource extends RestResource implements RestUsers {
	
	private final Users users;
	
	public UsersRestResource() {
		users = new UsersResource();
	}
	
	@Override
	public String createUser(User user) {
		return fromJavaResult(users.createUser(user));
	}
	
	@Override
	public User getUser(String userId, String password) {
		return fromJavaResult(users.getUser(userId, password));
	}
	
	
	@Override
	public User updateUser(String userId, String password, User updatedUser) {
		return fromJavaResult(users.updateUser(userId, password, updatedUser));
	}
	
	
	@Override
	public User deleteUser(String userId, String password) {
		return fromJavaResult(users.deleteUser(userId, password));
	}
	
	
	@Override
	public List<User> searchUsers(String pattern) {
		return fromJavaResult(users.searchUsers(pattern));
	}
	
}
