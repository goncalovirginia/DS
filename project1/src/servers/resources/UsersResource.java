package servers.resources;

import api.Result;
import api.User;
import api.Users;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UsersResource implements Users {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final Map<String, User> users;
	
	public UsersResource() {
		users = new HashMap<>();
	}
	
	@Override
	public Result<String> postUser(User user) {
		Log.info("postUser : " + user);
		
		Result<Void> r1 = validateUserObject(user);
		if (!r1.isOK()) return Result.error(r1.error());
		
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
		
		Result<User> r1 = validateUserCredentials(name, pwd);
		if (!r1.isOK()) return r1;
		
		r1.value().setNonNullAttributes(user);
		
		return r1;
	}
	
	
	@Override
	public Result<User> deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		
		Result<User> r1 = validateUserCredentials(name, pwd);
		if (!r1.isOK()) return r1;
		
		users.remove(name);
		
		return r1;
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
	
	private Result<Void> validateUserObject(User user) {
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			Log.info("Invalid user.");
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
		return Result.ok();
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
