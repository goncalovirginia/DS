package api.service;

import api.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path(RestUsers.PATH)
public interface RestUsers {
	
	public static final String PATH = "/users";
	public static final String QUERY = "query";
	public static final String USER_ID = "userId";
	public static final String PASSWORD = "password";
	
	/**
	 * Creates a new user.
	 * @param user User to be created.
	 * @return 200 and the userId. 409 if the userId already exists. 400 otherwise.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String createUser(User user);
	
	/**
	 * Obtains the information on the user identified by name.
	 * @param userId the userId of the user
	 * @param password password of the user
	 * @return 200 and the user object, if the userId exists and password matches the
	 * existing password; 403 if the password is incorrect; 404 if no user
	 * exists with the provided userId
	 */
	@GET
	@Path("/{" + USER_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
	User getUser(@PathParam(USER_ID) String userId, @QueryParam(PASSWORD) String password);
	
	/**
	 * Modifies the information of a user. Values of null in any field of the user
	 * will be considered as if the fields is not to be modified (the id cannot
	 * be modified).
	 * @param userId the userId of the user
	 * @param password password of the user
	 * @param user Updated information
	 * @return 200 the updated user object, if the name exists and password matches
	 * the existing password 403 if the password is incorrect 404 if no user
	 * exists with the provided userId 400 otherwise.
	 */
	@PUT
	@Path("/{" + USER_ID + "}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	User updateUser(@PathParam(USER_ID) String userId, @QueryParam(PASSWORD) String password, User user);
	
	/**
	 * Deletes the user identified by userId. The spreadsheets owned by the user
	 * should be eventually removed (asynchronous deletion is ok).
	 * @param userId the userId of the user
	 * @param password password of the user
	 * @return 200 the deleted user object, if the name exists and pwd matches the
	 * existing password 403 if the password is incorrect 404 if no user
	 * exists with the provided userId
	 */
	@DELETE
	@Path("/{" + USER_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
	User deleteUser(@PathParam(USER_ID) String userId, @QueryParam(PASSWORD) String password);
	
	/**
	 * Returns the list of users for which the pattern is a substring of the name
	 * (of the user), case-insensitive. The password of the users returned by the
	 * query must be set to the empty string "".
	 * @param pattern substring to search
	 * @return 200 when the search was successful, regardless of the number of hits
	 * (including 0 hits). 400 otherwise.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	List<User> searchUsers(@QueryParam(QUERY) String pattern);
	
}