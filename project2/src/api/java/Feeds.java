package api.java;

import api.Message;
import zookeeper.FeedsOperation;

import java.util.List;

public interface Feeds {

	/**
	 * Posts a new message in the feed, associating it to the feed of the specific user.
	 * A message should be identified before publish it, by assigning an ID.
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 *
	 * @param user user of the operation (format user@domain)
	 * @param msg  the message object to be posted to the server
	 * @param pwd  password of the user sending the message
	 * @return 200 the unique numerical identifier for the posted message;
	 * 404 if the publisher does not exist in the current domain
	 * 403 if the pwd is not correct
	 * 400 otherwise
	 */
	Result<Long> postMessage(String user, String pwd, Message msg);

	/**
	 * Removes the message identified by mid from the feed of user.
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 *
	 * @param user user feed being accessed (format user@domain)
	 * @param mid  the identifier of the message to be deleted
	 * @param pwd  password of the user
	 * @return 204 if ok
	 * 403 if the pwd is not correct
	 * 404 is generated if the message does not exist in the server or if the user does not exist
	 */
	Result<Void> removeFromPersonalFeed(String user, long mid, String pwd);

	/**
	 * Obtains the message with id from the feed of user (may be a remote user)
	 *
	 * @param user user feed being accessed (format user@domain)
	 * @param mid  id of the message
	 * @return 200 the message if it exists;
	 * 404 if the user or the message does not exist
	 */
	Result<Message> getMessage(String user, long mid);

	/**
	 * Returns a list of all messages stored in the server for a given user newer than time
	 * (note: may be a remote user)
	 *
	 * @param user user feed being accessed (format user@domain)
	 * @param time the oldest time of the messages to be returned
	 * @return 200 a list of messages, potentially empty;
	 * 404 if the user does not exist.
	 */
	Result<List<Message>> getMessages(String user, long time);


	/**
	 * Subscribe a user.
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 *
	 * @param user    the user subscribing (following) other user (format user@domain)
	 * @param userSub the user to be subscribed (followed) (format user@domain)
	 * @param pwd     password of the user
	 * @return 204 if ok
	 * 404 is generated if the user or the user to be subscribed does not exist
	 * 403 is generated if the pwd is not correct
	 */
	Result<Void> subUser(String user, String userSub, String pwd);

	/**
	 * UnSubscribe a user
	 * A user must contact the server of her domain directly (i.e., this operation should not be
	 * propagated to other domain)
	 *
	 * @param user    the user unsubscribing (following) other user (format user@domain)
	 * @param userSub the identifier of the user to be unsubscribed
	 * @param pwd     password of the user
	 * @return 204 if ok
	 * 404 is generated if the user or the user to be unsubscribed does not exist
	 * 403 is generated if the pwd is not correct
	 */
	Result<Void> unsubscribeUser(String user, String userSub, String pwd);

	/**
	 * Subscribed users.
	 *
	 * @param user user being accessed (format user@domain)
	 * @return 200 if ok
	 * 404 is generated if the user does not exist
	 */
	Result<List<String>> listSubs(String user);

	/**
	 * Adds the propagated message to the subscriber's feed in the current domain.
	 *
	 * @param message propagated message
	 * @param secret  secret string authenticating servers
	 * @return 204
	 */
	Result<Void> propagateMessage(Message message, String secret);

	/**
	 * Deletes all user data in the domain.
	 *
	 * @param user   user@domain
	 * @param secret secret string authenticating servers
	 * @return 204
	 */
	Result<Void> deleteUserData(String user, String secret);

	/**
	 * Replicates an operation from the primary server.
	 *
	 * @param operation operation to replicate
	 * @param secret    secret string authenticating servers
	 * @return 204, 403 if secret doesn't match, 409 if the operations' version is old
	 */
	Result<Void> replicateOperation(FeedsOperation operation, String secret);

}
