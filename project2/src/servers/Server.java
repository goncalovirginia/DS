package servers;

import tls.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.util.logging.Logger;

public abstract class Server {

    protected static Logger Log;
    protected final int port;
    protected final String service;
    protected final Class<?> resource;

    public static String domain;
    public static long serverId;
    public static String secret;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
    }

    protected Server(String className, int port, String service, Class<?> resource) {
        Log = Logger.getLogger(className);
        this.port = port;
        this.service = service;
        this.resource = resource;
    }

}
