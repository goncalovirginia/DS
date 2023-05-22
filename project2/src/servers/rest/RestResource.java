package servers.rest;

import api.java.Result;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import java.util.logging.Logger;

public abstract class RestResource {

	private static final Logger Log = Logger.getLogger(RestResource.class.getName());

	protected static <T> T fromJavaResult(Result<T> result) throws WebApplicationException {
		if (result.isOK()) {
			return result.value();
		}
		throw new WebApplicationException(Status.valueOf(result.error().name()));
	}

}
