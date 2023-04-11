package servers.resources;

import api.User;
import api.java.Result;
import api.java.Users;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class UsersResource implements Users {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final Map<String, User> users;
	
	public UsersResource() {
		users = new ConcurrentHashMap<>();
	}
	
	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			Log.info("Invalid user.");
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
		
		if (users.putIfAbsent(user.getName(), user) != null) {
			Log.info("User already exists.");
			return Result.error(Result.ErrorCode.CONFLICT);
		}
		
		return Result.ok(user.getName());
	}
	
	@Override
	public Result<User> getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		
		return validateUserCredentials(name, pwd);
	}
	
	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);
		
		Result<User> r = validateUserCredentials(name, pwd);
		if (!r.isOK()) return r;
		
		r.value().setNonNullAttributes(user);
		
		return r;
	}
	
	
	@Override
	public Result<User> deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		
		Result<User> r = validateUserCredentials(name, pwd);
		if (!r.isOK()) return r;
		
		users.remove(name);
		
		return r;
	}
	
	
	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		
		List<User> matches = new LinkedList<>();
		
		for (User user : users.values()) {
			if (user.getName().toLowerCase().contains(pattern)) {
				User userCopy = new User(user);
				userCopy.setPwd("");
				matches.add(userCopy);
			}
		}
		
		return Result.ok(matches);
	}
	
	private Result<User> validateUserCredentials(String userId, String password) {
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(userId);
		
		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
		
		if (!user.getPwd().equals(password)) {
			Log.info("Password is incorrect.");
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}
		
		return Result.ok(user);
	}
	
}
