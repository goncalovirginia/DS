package clients.soap;

import api.java.Result;
import api.soap.FeedsException;
import api.soap.UsersException;
import clients.Client;
import com.sun.xml.ws.client.BindingProviderProperties;
import jakarta.xml.ws.BindingProvider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public abstract class SoapClient extends Client {
	
	public static final String WSDL = "?wsdl";
	
	protected final URI serverURI;
	
	public SoapClient(URI serverURI) {
		this.serverURI = serverURI;
	}
	
	protected void setTimeouts(BindingProvider port) {
		port.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		port.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
	}
	
	protected interface ThrowsSupplier<T> {
		T get() throws Exception;
	}
	
	protected interface VoidThrowsSupplier {
		void run() throws Exception;
	}
	
	protected <T> Result<T> responseToResult(ThrowsSupplier<T> supplier) {
		try {
			return Result.ok(supplier.get());
		}
		catch (UsersException | FeedsException e) {
			return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
	
	protected <T> Result<T> responseToResult(VoidThrowsSupplier supplier) {
		try {
			supplier.run();
			return Result.ok();
		}
		catch (UsersException | FeedsException e) {
			return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
		}
		catch (Exception e) {
			Log.info(e.getMessage());
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
	
	public static URL toURL(String url) {
		try {
			return new URL(url);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
