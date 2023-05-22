package clients;

import jakarta.ws.rs.ProcessingException;
import jakarta.xml.ws.WebServiceException;
import tls.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class Client {

    protected static final Logger Log = Logger.getLogger(Client.class.getName());
    protected static final int READ_TIMEOUT = 10000, CONNECT_TIMEOUT = 10000, RETRY_SLEEP = 100, MAX_RETRIES = 3;

    protected <T> T reTry(Supplier<T> func) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return func.get();
            } catch (ProcessingException | WebServiceException e) {
                Log.info(e.getMessage());
                sleep(RETRY_SLEEP);
            } catch (Exception e) {
                Log.info(e.getMessage());
                break;
            }
        }
        return null;
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException x) {
            // nothing to do...
        }
    }

}
