package services;

import entities.Folder;
import util.APIGateway;
import util.ConfigurationHelper;

/**
 * A service for retrieving Folder data.
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
     *            the {@link Folder} DTOs
     * @return
     */
    public static Folder[] createFolders(long syncPointId, long folderId, Folder[] folders) {
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
     * @return the matching {@link Folder} object
     */
    public static Folder getFolder(long syncPointId, long folderId, boolean suppressErrors) {
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
    public static void deleteFolder(long syncPointId, long folderId) {
        httpDelete(String.format(folderUrl, syncPointId, folderId), Folder.class);
    }
}
