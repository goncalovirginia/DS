package server.resources;

import api.User;
import api.service.RestUsers;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class UsersResource implements RestUsers {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final Map<String, User> users = new HashMap<>();
	
	public UsersResource() {
	}
	
	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		
		validateUserObject(user);
		
		if (users.putIfAbsent(user.getUserId(), user) != null) {
			Log.info("User already exists.");
			throw new WebApplicationException(Status.CONFLICT);
		}
		
		return user.getUserId();
	}
	
	
	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		
		return validateUserCredentials(userId, password);
	}
	
	
	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
		
		validateUserCredentials(userId, password);
		validateUserObject(user);
		
		users.put(userId, user);
		
		return user;
	}
	
	
	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		
		validateUserCredentials(userId, password);
		return users.remove(userId);
	}
	
	
	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		
		List<User> matches = new LinkedList<>();
		
		for (User user : users.values()) {
			if (user.getUserId().toLowerCase().contains(pattern)) {
				User userCopy = new User(user);
				userCopy.setPassword("");
				matches.add(userCopy);
			}
		}
		
		return matches;
	}
	
	private void validateUserObject(User user) {
		if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || user.getEmail() == null) {
			Log.info("Invalid user.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
	}
	
	private User validateUserCredentials(String userId, String password) {
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		
		User user = users.get(userId);
		
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		
		return user;
	}
	
}
