package api.rest;

import api.Message;
import api.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path(FeedsService.PATH)
public interface FeedsService {
	
	String PATH = "/feeds";
	
	/**
	 * Posts a new message in the feed, associating it to the feed of the specific user.
	 * A message should be identified before publish it, by assigning an ID.
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 * @param user user of the operation (format user@domain)
	 * @param msg the message object to be posted to the server
	 * @param pwd password of the user sending the message
	 * @return 200 the unique numerical identifier for the posted message;
	 * 403 if the publisher does not exist in the current domain or if the pwd is not correct
	 * 400 otherwise
	 */
	@POST
	@Path("/{user}@{domain}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	long postMessage(@PathParam("user") String user, @QueryParam("pwd") String pwd, Message msg);
	
	/**
	 * Removes the message identified by mid from the feed of user.
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 * @param user user feed being accessed (format user@domain)
	 * @param mid the identifier of the message to be deleted
	 * @param pwd password of the user
	 * @return 204 if ok
	 * 403 if the user does not exist or if the pwd is not correct;
	 * 404 is generated if the message does not exist in the server.
	 */
	@DELETE
	@Path("/{user}/{mid}")
	void removeFromPersonalFeed(@PathParam("user") String user, @PathParam("mid") long mid,
	                            @QueryParam("pwd") String pwd);
	
	/**
	 * Obtains the message with id from the feed of user (may be a remote user)
	 * @param user user feed being accessed (format user@domain)
	 * @param mid id of the message
	 * @return 200 the message if it exists;
	 * 404 if the user or the message does not exists
	 */
	@GET
	@Path("/{user}/{mid}")
	@Produces(MediaType.APPLICATION_JSON)
	Message getMessage(@PathParam("user") String user, @PathParam("mid") long mid);
	
	/**
	 * Returns a list of all messages stored in the server for a given user newer than time
	 * (note: may be a remote user)
	 * @param user user feed being accessed (format user@domain)
	 * @param time the oldest time of the messages to be returned
	 * @return 200 a list of messages, potentially empty;
	 * 404 if the user does not exist.
	 */
	@GET
	@Path("/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	List<Message> getMessages(@PathParam("user") String user, @QueryParam("time") long time);
	
	
	/**
	 * Subscribe a user.
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 * @param user the user subscribing (following) other user (format user@domain)
	 * @param userSub the user to be subscribed (followed) (format user@domain)
	 * @param pwd password of the user to subscribe
	 * @return 200 if ok
	 * 404 is generated if the user to be subscribed does not exist
	 * 403 is generated if the user does not exist or if the pwd is not correct
	 */
	@POST
	@Path("/sub/{user}/{userSub}")
	@Produces(MediaType.APPLICATION_JSON)
	void subUser(@PathParam("user") String user, @PathParam("userSub") long userSub,
	             @QueryParam("pwd") String pwd);
	
	/**
	 * UnSubscribe a user
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 * @param user the user unsubscribing (following) other user (format user@domain)
	 * @param userSub the identifier of the user to be unsubscribed
	 * @param pwd password of the user to subscribe
	 * @return 200 if ok
	 * 403 is generated if the user does not exist or if the pwd is not correct
	 * 404 is generated if the userSub is not subscribed
	 */
	@DELETE
	@Path("/sub/{user}/{userSub}")
	@Produces(MediaType.APPLICATION_JSON)
	void unsubscribeUser(@PathParam("user") String user, @PathParam("userSub") String userSub,
	                     @QueryParam("pwd") String pwd);
	
	
	/**
	 * Subscribed users.
	 * @param user user being accessed (format user@domain)
	 * @return 200 if ok
	 * 404 is generated if the user does not exist
	 */
	@GET
	@Path("/sub/list/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	List<User> listSubs(@PathParam("user") String user);
}
