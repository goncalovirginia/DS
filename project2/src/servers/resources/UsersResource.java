package servers.resources;

import api.User;
import api.java.Result;
import api.java.Result.ErrorCode;
import api.java.Users;
import clients.FeedsClientFactory;
import discovery.DiscoverySingleton;
import servers.Server;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UsersResource implements Users {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	private final Map<String, User> users;
	
	public UsersResource() {
		users = new ConcurrentHashMap<>();
	}
	
	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		if (userObjectInvalid(user)) {
			Log.info("Invalid user.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		if (users.putIfAbsent(user.getName(), user) != null) {
			Log.info("User already exists.");
			return Result.error(ErrorCode.CONFLICT);
		}
		
		return Result.ok(user.getName() + "@" + user.getDomain());
	}
	
	@Override
	public Result<User> getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		
		return validateUserCredentials(name, pwd);
	}
	
	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);
		
		if (user.getName() != null && !name.equals(user.getName())) {
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
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
		
		String nameAndDomain = name + "@" + Server.domain;
		
		threadPool.execute(() -> FeedsClientFactory.get(DiscoverySingleton.getInstance().getURI(Server.domain + ":feeds"))
				.deleteUserData(nameAndDomain, Server.secret));
		
		for (URI uri : DiscoverySingleton.getInstance().getURIsOfOtherDomainsFeeds(Server.domain)) {
			threadPool.execute(() -> FeedsClientFactory.get(uri).deleteUserData(nameAndDomain, Server.secret));
		}
		
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
	
	private boolean userObjectInvalid(User user) {
		return user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null;
	}
	
	private Result<User> validateUserCredentials(String name, String password) {
		if (name == null || password == null) {
			Log.info("name or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(name);
		
		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		
		if (!user.getPwd().equals(password)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		
		return Result.ok(user);
	}
	
}
