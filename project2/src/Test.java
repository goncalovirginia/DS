import api.Message;
import api.java.Result;
import mastodon.Mastodon;

import java.util.List;

public class Test {
	public static void main(String[] args) {
		//var res0 = Mastodon.getInstance().postMessage("", "", new Message(0, "", "", "test ;;;; " + System.currentTimeMillis()));
		//System.out.println(res0);

		Result<List<Message>> res1 = Mastodon.getInstance().getMessages("", 0);
		System.out.println(res1);

		Result<Message> res2 = Mastodon.getInstance().getMessage("", 110397195144638569L);
		System.out.println(res2);

		Result<Void> res3 = Mastodon.getInstance().removeFromPersonalFeed("", 110397195144638569L, "");
		System.out.println(res3);
	}
}
