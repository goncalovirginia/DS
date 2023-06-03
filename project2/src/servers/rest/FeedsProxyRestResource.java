package servers.rest;

import jakarta.inject.Singleton;
import mastodon.Mastodon;

@Singleton
public class FeedsProxyRestResource extends FeedsRestResource {

	public FeedsProxyRestResource() {
		this.feeds = new Mastodon();
	}

}
