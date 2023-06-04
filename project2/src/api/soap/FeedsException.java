package api.soap;

import jakarta.xml.ws.WebFault;

@WebFault
public class FeedsException extends Exception {

    private static final long serialVersionUID = 1L;

    public FeedsException() {
        super("");
    }

    public FeedsException(String errorMessage) {
        super(errorMessage);
    }

}
