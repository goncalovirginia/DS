package clients.soap;

import api.java.Result;
import api.soap.FeedsException;
import api.soap.UsersException;
import clients.Client;

public abstract class SoapClient extends Client {
	
	protected interface ThrowsSupplier<T> {
		T get() throws Exception;
	}
	
	protected interface VoidThrowsSupplier {
		void get() throws Exception;
	}
	
	protected <T> Result<T> responseToResult(ThrowsSupplier<T> func) {
		try {
			return Result.ok(func.get());
		}
		catch (UsersException | FeedsException e) {
			return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return null;
		}
	}
	
	protected <T> Result<T> voidResponseToResult(VoidThrowsSupplier func) {
		try {
			func.get();
			return Result.ok();
		}
		catch (UsersException | FeedsException e) {
			return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return null;
		}
	}
	
}
