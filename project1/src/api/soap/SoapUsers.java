package api.soap;

import api.User;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

import java.util.List;

@WebService(serviceName = SoapUsers.NAME, targetNamespace = SoapUsers.NAMESPACE, endpointInterface = SoapUsers.INTERFACE)
public interface SoapUsers {
	
	String NAME = "users";
	String NAMESPACE = "http://sd2223";
	String INTERFACE = "api.soap.RestUsers";
	
	/**
	 * Creates a new user identified by name.
	 * @param user user to be created
	 * @throws UsersException otherwise
	 */
	@WebMethod
	String createUser(User user) throws UsersException;
	
	/**
	 * Obtains the information on the user identified by name.
	 * @param name name of the user
	 * @param pwd password of the user
	 * @throws UsersException otherwise
	 */
	@WebMethod
	User getUser(String name, String pwd) throws UsersException;
	
	/**
	 * Modifies the information of a user. Values of null in any field of the user will be
	 * considered as if the fields is not to be modified (the id cannot be modified).
	 * @param name name of the user
	 * @param pwd password of the user
	 * @param user Updated information
	 * @throws UsersException otherwise
	 */
	@WebMethod
	User updateUser(String name, String pwd, User user) throws UsersException;
	
	/**
	 * Deletes the user identified by userId. The spreadsheets owned by the user should be eventually removed
	 * (asynchronous
	 * deletion is ok).
	 * @param name name of the user
	 * @param pwd password of the user
	 * @throws UsersException otherwise
	 */
	@WebMethod
	User deleteUser(String name, String pwd) throws UsersException;
	
	/**
	 * Returns the list of users for which the pattern is a substring of the name (of the user), case-insensitive.
	 * The password of the users returned by the query must be set to the empty string "".
	 * @param pattern substring to search
	 * @throws UsersException otherwise
	 */
	@WebMethod
	List<User> searchUsers(String pattern) throws UsersException;
	
}
