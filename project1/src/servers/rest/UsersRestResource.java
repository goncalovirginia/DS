package servers.rest;

import api.User;
import api.Users;
import api.rest.RestUsers;
import jakarta.inject.Singleton;
import servers.resources.UsersResource;

import java.util.List;

@Singleton
public class UsersRestResource implements RestUsers {
	
	private final Users users;
	
	public UsersRestResource() {
		users = new UsersResource();
	}
	
	@Override
	public String postUser(User user) {
		return RestResource.processResult(users.postUser(user));
	}
	
	@Override
	public User getUser(String userId, String password) {
		return RestResource.processResult(users.getUser(userId, password));
	}
	
	
	@Override
	public User updateUser(String userId, String password, User updatedUser) {
		return RestResource.processResult(users.updateUser(userId, password, updatedUser));
	}
	
	
	@Override
	public User deleteUser(String userId, String password) {
		return RestResource.processResult(users.deleteUser(userId, password));
	}
	
	
	@Override
	public List<User> searchUsers(String pattern) {
		return RestResource.processResult(users.searchUsers(pattern));
	}
	
}
