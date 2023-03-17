package servers.soap;

import api.Result;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public abstract class SoapResource {
	
	private static final Logger Log = Logger.getLogger(SoapResource.class.getName());
	
	protected static <T, E extends Exception> T processResult(Result<T> result, Class<E> desiredException) throws E {
		try {
			if (result.isOK()) {
				return result.value();
			}
			throw desiredException.getConstructor(String.class).newInstance(result.error().name());
		}
		catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			Log.info(e.getMessage());
			return null;
		}
	}
	
}
