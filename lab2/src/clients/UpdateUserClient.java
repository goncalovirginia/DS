package clients;

import api.User;

import java.io.IOException;

public class UpdateUserClient {
	
	public static void main(String[] args) throws IOException {
		
		if (args.length != 6) {
			System.err.println("Use: java aula2.clients.UpdateUserClient url userId oldpwd fullName email password");
			return;
		}
		
		String serverUrl = args[0];
		String userId = args[1];
		String oldpwd = args[2];
		String fullName = args[3];
		String email = args[4];
		String password = args[5];
		
		var u = new User(userId, fullName, email, password);
		
		System.out.println("Sending request to server.");
		
		//TODO complete this client code
	}
	
}
