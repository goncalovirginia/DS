package clients.soap;

import clients.Client;
import sd2122.tp1.api.service.soap.DirectoryException;
import sd2122.tp1.api.service.soap.FilesException;
import sd2122.tp1.api.service.soap.UsersException;
import sd2122.tp1.api.service.util.Result;

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
		catch (UsersException | DirectoryException | FilesException e) {
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
		catch (UsersException | DirectoryException | FilesException e) {
			return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return null;
		}
	}
	
}
