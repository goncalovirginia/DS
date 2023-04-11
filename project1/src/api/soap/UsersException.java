package api.soap;

import jakarta.xml.ws.WebFault;
import jakarta.xml.ws.WebServiceException;

@WebFault
public class UsersException extends WebServiceException {
	
	
	public UsersException() {
		super("");
	}
	
	public UsersException(String errorMessage) {
		super(errorMessage);
	}
	
	private static final long serialVersionUID = 1L;
}
