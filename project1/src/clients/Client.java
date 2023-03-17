package clients;

import jakarta.ws.rs.ProcessingException;
import jakarta.xml.ws.WebServiceException;

import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class Client {
	
	protected static final Logger Log = Logger.getLogger(Client.class.getName());
	protected static final int READ_TIMEOUT = 10000, CONNECT_TIMEOUT = 10000, RETRY_SLEEP = 1000, MAX_RETRIES = 3;
	
	private int retries = MAX_RETRIES;
	
	public void setRetries(int retries) {
		this.retries = retries;
	}
	
	protected <T> T reTry(Supplier<T> func) {
		for (int i = 0; i < retries; i++) {
			try {
				return func.get();
			}
			catch (ProcessingException | WebServiceException e) {
				Log.info(e.getMessage());
				sleep(RETRY_SLEEP);
			}
			catch (Exception e) {
				Log.info(e.getMessage());
				break;
			}
		}
		return null;
	}
	
	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException x) {
			// nothing to do...
		}
	}
	
}
