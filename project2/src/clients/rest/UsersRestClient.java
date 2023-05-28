package clients.rest;

import api.User;
import api.java.Result;
import api.java.Users;
import api.rest.RestUsers;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

public class UsersRestClient extends RestClient implements Users {
	
	public UsersRestClient(URI serverURI) {
		super(serverURI, RestUsers.PATH);
	}
	
	@Override
	public Result<String> createUser(User user) {
		return reTry(() -> clt_postUser(user));
	}
	
	@Override
	public Result<User> getUser(String name, String pwd) {
		return reTry(() -> clt_getUser(name, pwd));
	}
	
	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		return reTry(() -> clt_updateUser(name, pwd, user));
	}
	
	@Override
	public Result<User> deleteUser(String name, String pwd) {
		return reTry(() -> clt_deleteUser(name, pwd));
	}
	
	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return reTry(() -> clt_searchUsers(pattern));
	}
	
	private Result<String> clt_postUser(User user) {
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));
		
		return responseToResult(r, String.class);
	}
	
	private Result<User> clt_getUser(String name, String pwd) {
		Response r = target.path(name)
				.queryParam("pwd", pwd)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return responseToResult(r, User.class);
	}
	
	private Result<User> clt_updateUser(String name, String pwd, User user) {
		Response r = target.path(name)
				.queryParam("pwd", pwd)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));
		
		return responseToResult(r, User.class);
	}
	
	private Result<User> clt_deleteUser(String name, String pwd) {
		Response r = target.path(name)
				.queryParam("pwd", pwd)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		
		return responseToResult(r, User.class);
	}
	
	private Result<List<User>> clt_searchUsers(String pattern) {
		Response r = target
				.queryParam("query", pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return responseToResult(r, new GenericType<List<User>>() {
		});
	}
	
}
