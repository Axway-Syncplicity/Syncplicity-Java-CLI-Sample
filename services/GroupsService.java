package services;

import util.APIGateway;
import entities.Group;

import java.util.UUID;


/**
 * Class for requests to group.svc and groups.svc
 */
public class GroupsService 
	extends APIGateway {


	/**
	 * Gets or sets url to Groups service.
	 */
	protected static String groupsUrl;

	/**
	 * Gets or sets url to Group service.
	 */
	protected static String groupUrl;

	static {
		groupsUrl = provisioningAPIUrlPrefix + "groups.svc/%s/groups";
		groupUrl = provisioningAPIUrlPrefix + "group.svc/%s";
	}

	/**
	 * Creates new groups in company.
	 * 
	 * @param companyGuid Company Guid.
	 * @param groups Array of groups to be created.
	 * 
	 * @return Array of created groups.
	 */
	public static Group[] createGroups(String companyGuid, Group[] groups) {
		return httpPost(String.format(groupsUrl, companyGuid), "application/json", groups );
	}


	/**
	 * Deletes group by Guid.
	 * 
	 * @param groupGuid Group Guid.
	 */
	public static void deleteGroup(UUID groupGuid) {
		httpDelete(String.format(groupUrl, groupGuid), Group.class);
	}
}
