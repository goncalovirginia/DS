package servers.resources;

import api.Message;
import api.Result;
import api.User;
import api.java.Feeds;
import clients.UsersClientFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FeedsResource implements Feeds {
	
	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	private final Map<String, Map<Long, Message>> userMessages;
	
	public FeedsResource() {
		userMessages = new ConcurrentHashMap<>();
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		Result<Void> messageValidation = validateMessageObject(msg);
		if (!messageValidation.isOK()) return Result.error(messageValidation.error());
		
		String[] nameAndDomain = user.split("@");
		
		Result<User> userResult = checkUsersServer(nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(userResult.error());
		
		Message newMsg = new Message(msg);
		userMessages.get(user).put(newMsg.getId(), newMsg);
		
		return Result.ok(newMsg.getCreationTime());
	}
	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		String[] nameAndDomain = user.split("@");
		
		Result<User> userResult = checkUsersServer(nameAndDomain[0], pwd);
		if (!userResult.isOK()) return Result.error(Result.ErrorCode.FORBIDDEN);
		
		Message removedMsg = userMessages.get(nameAndDomain[0]).remove(mid);
		if (removedMsg == null) return Result.error(Result.ErrorCode.NOT_FOUND);
		
		return Result.ok();
	}
	
	@Override
	public Result<Message> getMessage(String user, long mid) {
		String[] nameAndDomain = user.split("@");
		
		Message msg = userMessages.get(nameAndDomain[0]).get(mid);
		if (msg == null) return Result.error(Result.ErrorCode.NOT_FOUND);
		
		return Result.ok(msg);
	}
	
	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		return Result.ok();
	}
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		return Result.ok();
	}
	
	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		return Result.ok();
	}
	
	@Override
	public Result<List<User>> listSubs(String user) {
		return Result.ok();
	}
	
	private Result<Void> validateMessageObject(Message message) {
		if (message.getUser() == null || message.getDomain() == null || message.getText() == null) {
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
		
		return Result.ok();
	}
	
	private Result<User> checkUsersServer(String userId, String password) {
		try {
			return UsersClientFactory.getClient().getUser(userId, password);
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}
	}
	
}
