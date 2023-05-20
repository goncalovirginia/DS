package mastodon.msgs;

import api.Message;

public record PostStatusResult(String id, String content, String created_at, MastodonAccount account) {

    public long getId() {
        return Long.parseLong(id);
    }

    long getCreationTime() {
        return 0;
    }

    public String getText() {
        return content;
    }

    public Message toMessage() {
        var m = new Message(getId(), account.id(), "todo", getText());
        m.setCreationTime(getCreationTime());
        return m;
    }

}