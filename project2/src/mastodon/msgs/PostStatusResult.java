package mastodon.msgs;

import api.Message;
import servers.Server;

import java.time.Instant;

public record PostStatusResult(String id, String content, String created_at, MastodonAccount account) {

	public long getId() {
		return Long.parseLong(id);
	}

	long getCreationTime() {
		return Instant.parse(created_at).toEpochMilli();
	}

	public String getText() {
		return content.replaceAll("\\<[^>]*>", "");
	}

	public Message toMessage() {
		var m = new Message(getId(), account.username(), Server.domain, getText());
		m.setCreationTime(getCreationTime());
		return m;
	}

}