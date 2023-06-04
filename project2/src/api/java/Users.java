package api.java;

import api.User;

import java.util.List;

public interface Users {

    Result<String> createUser(User user);

    Result<User> getUser(String name, String pwd);

    Result<User> updateUser(String name, String pwd, User user);

    Result<User> deleteUser(String name, String pwd);

    Result<List<User>> searchUsers(String pattern);

}
