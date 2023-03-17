package server.resources;

import api.User;
import api.rest.UsersService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class UsersResource implements UsersService {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private static final Map<String, User> users = new HashMap<>();
	
	public UsersResource() {
	
	}
	
	@Override
	public String postUser(User user) {
		Log.info("createUser : " + user);
		
		validateUserObject(user);
		
		if (users.putIfAbsent(user.getName(), user) != null) {
			Log.info("User already exists.");
			throw new WebApplicationException(Status.CONFLICT);
		}
		
		return user.getName();
	}
	
	@Override
	public User getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		
		return validateUserCredentials(name, pwd);
	}
	
	
	@Override
	public User updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);
		
		validateUserCredentials(name, pwd);
		validateUserObject(user);
		
		users.put(name, user);
		
		return user;
	}
	
	
	@Override
	public User deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		
		validateUserCredentials(name, pwd);
		return users.remove(name);
	}
	
	
	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		
		List<User> matches = new LinkedList<>();
		
		for (User user : users.values()) {
			if (user.getName().toLowerCase().contains(pattern)) {
				User userCopy = new User(user);
				userCopy.setPwd("");
				matches.add(userCopy);
			}
		}
		
		return matches;
	}
	
	private void validateUserObject(User user) {
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
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
		
		if (!user.getPwd().equals(password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		
		return user;
	}
	
}
