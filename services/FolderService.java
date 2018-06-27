package services;

import entities.Folder;
import util.APIGateway;
import util.ConfigurationHelper;

/**
 * A service for retrieving Folder data.
 *
 */
public class FolderService extends APIGateway {

    /**
     * Folders service URL.
     */
    protected static String foldersUrl;

    /**
     * Folder service URL.
     */
    protected static String folderUrl;

    static {
        foldersUrl = ConfigurationHelper.getBaseApiEndpointUrl() + "sync/folder_folders.svc/%s/folder/%s/folders";
        folderUrl = ConfigurationHelper.getBaseApiEndpointUrl() + "sync/folder.svc/%s/folder/%s?include=active";
    }

    /**
     * Creates folders.
     * 
     * @param syncPointId
     *            the SyncPoint ID
     * @param folderId
     *            the parent Folder ID
     * @param folders
     *            the <code>Folder</code> DTOs
     * @return
     */
    public static Folder[] createFolders(int syncPointId, String folderId, Folder[] folders) {
        return httpPost(String.format(foldersUrl, syncPointId, folderId), "application/json", folders);
    }

    /**
     * Retrieves a Folder.
     * 
     * @param syncPointId
     *            the SyncPoint ID
     * @param folderId
     *            the Folder ID
     * @param suppressErrors
     *            indicates whether errors should be suppressed
     * @return the matching <code>Folder</code> object
     */
    public static Folder getFolder(long syncPointId, String folderId, boolean suppressErrors) {
        return httpGet(String.format(folderUrl, syncPointId, folderId), Folder.class, suppressErrors);
    }

    /**
     * Deletes a Folder.
     * 
     * @param syncPointId
     *            the SyncPoint ID
     * @param folderId
     *            the Folder ID
     */
    public static void deleteFolder(long syncPointId, String folderId) {
        httpDelete(String.format(folderUrl, syncPointId, folderId), Folder.class);
    }
}
