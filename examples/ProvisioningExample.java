package examples;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import services.GroupMembersService;
import services.GroupsService;
import services.UsersService;
import util.APIContext;
import util.ConfigurationHelper;
import entities.Group;
import entities.User;
import entities.UserAccountType;

public class ProvisioningExample {

	private static int NewUsersCount = 5;
	
	private static User[] createdUsers;
	private static User[] groupMemberUsers;
	private static Group  createdGroup;
	
	/*
	 * Provisioning
     * - Creating new users associated with a company
     * - Creating a new user group (a group as defined in Syncplicity as having access to the same shared folders)
     * - Associating the newly created users with the new user group
     * - Removing the users from the group
     * - Deleting the group
	 */
	public static void execute( ) {
		createUsers();
		createGroup();
		addUsersToGroup();
		removeUsersFromGroup();
		deleteGroup();
		deleteUsers();
	}
	
	private static void deleteGroup() {
		//Remove the previously created group
		if( createdGroup == null ) {
			return;
		}
		
		System.out.println("");
		System.out.println("Press enter before Group Deletion...");
		
		System.out.println("");
		System.out.println(String.format("Delete Group With Id %s", createdGroup.Id));
		GroupsService.deleteGroup(createdGroup.Id);
	}
	
	private static void removeUsersFromGroup() {

		if( groupMemberUsers == null || groupMemberUsers.length == 0 || createdGroup == null ) {
			return;
		}
		
		System.out.println("");
		System.out.println("Press enter before Group Members Deletion...");
		
		// remove selected group member
		System.out.println("");
		System.out.println("Start Group Member Deletion...");

		for( User user : groupMemberUsers ) {
			System.out.println("");
			System.out.println( String.format( "Removing user[%s] from group [%s].", user.EmailAddress, createdGroup.Id ));
			GroupMembersService.deleteGroupMember(
					createdGroup.Id,
					user.EmailAddress);
		}

		System.out.println("");
		System.out.println("Finish Group Member Deletion.");		
	}
	
	private static void addUsersToGroup() {

		if( createdUsers == null || createdUsers.length == 0 || createdGroup == null ) {
			return;
		}
		
		System.out.println("");
		System.out.println("Start Adding Group Members...");
		
		groupMemberUsers = GroupMembersService.addGroupMembers( 
							  createdGroup.Id,
							  createdUsers
						   );

		System.out.println("");
		if (groupMemberUsers == null || groupMemberUsers.length == 0) {

			int addedCount = (groupMemberUsers == null ? 0 : groupMemberUsers.length);
			System.err.println(String.format("Error Occured During Adding Some Of Members. Number of Added Members:%d", addedCount));
				
			return;
		}

		System.out.println("Finish Adding Group Members.");
	}
	
	private static void createGroup() {

		System.out.println("");

		String companyGuid = APIContext.getCompanyGuid();
        if (companyGuid == null || companyGuid.isEmpty())
        {
        	System.err.println("Group Can't Be Created Because Company Guid Wasn't Received During Authorization.");
            return;
        }
		
		System.out.println("Start Group Creation...");

		Group group = new Group();
		group.Name = ConfigurationHelper.getGroupName() + new Date().getTime();  //Use timestamp to generate unique name
		group.Owner = new User();
		group.Owner.EmailAddress = ConfigurationHelper.getOwnerEmail();

		Group[] createdGroups = GroupsService.createGroups(companyGuid, new Group[] { group });
		
		System.out.println("");
		
		if (createdGroups == null || createdGroups.length == 0) {
			System.err.println("Error Occured During Creating Group.");
			return;
		}

		createdGroup = createdGroups[0];
		
		System.out.println(String.format("Finished Group Creation. New Group Id: %s", createdGroup.Id ));
	}
	
	private static void createUsers() {	

		String baseEmail = String.format("fake-user-%s@fakedomain.com", Long.toString(new Date().getTime()).substring(0, 7));
		
		// create users with email typed by user + sequence number
		System.out.println("");
		System.out.println("Start Users Creation...");
		
		String[] emailParts = baseEmail.split("@");

		List<User> usersToAdd = new ArrayList<User>();

		for (int i = 1; i <= NewUsersCount; i++) {
			
			User user = new User();
			user.AccountType = UserAccountType.LimitedBusiness;
			user.EmailAddress = String.format("%s-%d@%s", emailParts[0], i, emailParts[1]);
			user.Password = ConfigurationHelper.getSimplePassword();			
			user.FirstName = String.format("%s-%d", emailParts[0], i);
			user.LastName = String.format("Lastname %d", i);

			usersToAdd.add(user);
			System.out.println("");			
			System.out.println(String.format("Preparing User #%d [%s].", i, user.EmailAddress));
			System.out.println(String.format("\tChecking if user [%s] already exists.", user.EmailAddress));
			//Checking if we've created this user already, if so, we'll delete it, and then recreate it again (below).
			User checkUser = UsersService.getUser( user.EmailAddress, true );
			
			System.out.println("");
			
			if( checkUser != null ) {
				//If this is the second time running, we'll need to clean up (delete) previous run's users
				//This is just to keep the sample code working as if it was the first time run...
				System.out.println( String.format("\tUser found. Cleanup of User #%d [%s].  Deleting user that may have been created from previous Sample App run.", i, user.EmailAddress) );
				try {
					UsersService.deleteUser(user.EmailAddress);
				}
				catch( Exception e ) { 
					//ignore exceptions for sample app only
				}
			}
			else {
				System.out.println( "\tNo user found in company, continuing successfully." );
			}
		}

		System.out.println("");
		System.out.println( "Calling UsersService to add users" );
		createdUsers = UsersService.createUsers(usersToAdd.toArray(new User[usersToAdd.size()]));

		System.err.println("");
		if (createdUsers == null || createdUsers.length == 0 ) {

			int addedCount = (createdUsers == null ? 0 : createdUsers.length);

			System.err.println( String.format("\tError Occured During Creating Some Of Users. Number of Created Users:%d", addedCount) );
		}
		else {
			System.out.println("\tFinished Users Creation.");
		}
	}
	
	private static void deleteUsers() {	

		if ( createdUsers == null || createdUsers.length == 0 ) {
			return;
		}
		
		System.out.println("");
		System.out.println("Press enter before Users Deletion...");
		
		// remove created users
		System.out.println("Start Users Deletion...");

		for( User user : createdUsers ) {

			System.out.println("");
			System.out.println(String.format("\tDelete User [%s].",user.EmailAddress));
			
			UsersService.deleteUser(user.EmailAddress);
		}

		System.out.println("");
		System.out.println("Finished Users Deletion.");
	}
}
