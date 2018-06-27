package services;

import util.APIGateway;
import entities.User;

/**
 * Class for requests to users.svc, users_public.svc and user.svc
 *
 */
public class UsersService 
	extends APIGateway {

	/**
	 * Gets or sets url to Users service.
	 */
	private static String usersUrl;

	/**
	 * Gets or sets url to User service.
	 */
	private static String userUrl;

	private static String usersPublicUrl;
	
	static {
		usersUrl = provisioningAPIUrlPrefix + "users.svc/";
		usersPublicUrl = provisioningAPIUrlPrefix + "users_public.svc/";
		userUrl = provisioningAPIUrlPrefix + "user.svc/%s";
	}
	/**
	 * Creates new users for current company.
	 * 
	 * @param users Array of users to be created.
	 * 
	 * @return Array of created users.
	 */
	public static User[] createUsers(User[] users) {
		return httpPost(usersUrl, "application/json", users);
	}

	/**
	 * Deletes user by email.
	 * 
	 * @param email Email of deleted user.
	 */
	public static void deleteUser(String email) {
		httpDelete(String.format(userUrl, email), User.class);
	}
	
	/**
	 * Retrieves user by email.
	 * 
	 * @param email Email of user to be retrieved.
	 */
	public static User getUser(String email, boolean suppressErrors ) {
		return httpGet(String.format(userUrl, email), User.class, suppressErrors);
	}
}
