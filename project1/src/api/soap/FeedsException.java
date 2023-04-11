package api.soap;

import jakarta.xml.ws.WebFault;
import jakarta.xml.ws.WebServiceException;

@WebFault
public class FeedsException extends WebServiceException {
	
	
	public FeedsException() {
		super("");
	}
	
	public FeedsException(String errorMessage) {
		super(errorMessage);
	}
	
	private static final long serialVersionUID = 1L;
}
