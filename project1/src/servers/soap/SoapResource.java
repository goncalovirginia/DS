package servers.soap;

import api.java.Result;

import java.util.function.Function;

public abstract class SoapResource<E extends Throwable> {
	
	Function<Result<?>, E> exceptionMapper;
	
	SoapResource(Function<Result<?>, E> exceptionMapper) {
		this.exceptionMapper = exceptionMapper;
	}
	
	/*
	 * Given a Result<T> returns T value or throws an exception created using the given function.
	 */
	<T> T fromJavaResult(Result<T> result) throws E {
		if (result.isOK()) {
			return result.value();
		}
		throw exceptionMapper.apply(result);
	}
	
}
