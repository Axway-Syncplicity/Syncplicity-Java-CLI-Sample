package examples;

import entities.File;
import entities.Folder;
import entities.FolderStatus;
import entities.Link;
import entities.StorageEndpoint;
import entities.SyncPoint;
import entities.SyncPointType;
import org.apache.commons.lang.StringUtils;
import services.FileService;
import services.FolderService;
import services.LinkService;
import services.StorageEndpointService;
import services.SyncPointService;
import util.ConfigurationHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Provides Syncplicity Content API usage samples.
 */
public class ContentExample {

    private static SyncPoint createdSyncPoint;
    private static Folder createdFolder;
    private static File uploadedFile;
    private static Link generatedLink;

    /**
     * Samples:
     * <UL>
     * <LI>Retrieve Storage Endpoints.
     * <LI>Retrieve SyncPoint.
     * <LI>Create a SyncPoint
     * <LI>Create a Folder
     * <LI>Retrieve a Folder
     * <LI>Upload a File.
     * <LI>Retrieve File info.
     * <LI>Download a File.
     * <LI>Generate a Share Link.
     * <LI>Retrieve a Share Link.
     * <LI>Delete a Share Link.
     * <LI>Delete a Folder.
     * <LI>Delete a SyncPoint.
     * </UL>
     */
    public static void execute() {
        getStorageEndpoints();
        getSyncPoints();
        createSyncPoint();
        createFolder();
        getFolder();
        uploadFile();
        getFile();
        downloadFile();
        generateLink();
        getLink();
        deleteLink();
        deleteFolder();
        deleteSyncPoint();
    }

    private static void getStorageEndpoints() {
        SyncPoint[] syncPoints = SyncPointService.getSyncPoints(true);
        System.out.println(String.format("Retrieved %s storage endpoints.", syncPoints.length));
    }

    private static void getSyncPoints() {
        SyncPoint[] syncPoints = SyncPointService.getSyncPoints(true);
        System.out.println(String.format("Retrieved %s syncpoints.", syncPoints.length));
    }

    private static void createSyncPoint() {
        System.out.println("Starting SyncPoint creation..");
        StorageEndpoint[] storageEndpoints = StorageEndpointService.getStorageEndpoints(true);
        if (storageEndpoints.length == 0) {
            System.err.println("Cannot create a syncpoint as the user does not have storage endpoints.");
            return;
        }
        StorageEndpoint defaultStorageEndpoint = Arrays.stream(storageEndpoints)
                .filter(se -> se.Default && se.Active)
                .findFirst()
                .orElse(storageEndpoints[0]);

        SyncPoint syncPoint = new SyncPoint();
        syncPoint.Type = SyncPointType.Custom;
        syncPoint.Name = "NewSyncPoint";
        syncPoint.Mapped = true;
        syncPoint.DownloadEnabled = true;
        syncPoint.UploadEnabled = true;
        syncPoint.Path = "";
        syncPoint.StorageEndpointId = defaultStorageEndpoint.Id;
        SyncPoint[] syncPoints = { syncPoint };
        SyncPoint[] createdSyncPoints = SyncPointService.createSyncPoints(syncPoints);
        if (createdSyncPoints == null || createdSyncPoints.length == 0) {
            System.err.println("An error occurred during SyncPoint creation.");
            return;
        }
        createdSyncPoint = createdSyncPoints[0];
        System.out.println(String.format("Finished SyncPoint Creation. New SyncPoint id: %s", createdSyncPoint.Id));
    }

    private static void deleteSyncPoint() {
        System.out.println("Starting SyncPoint deleting..");
        if (createdSyncPoint != null) {
            SyncPointService.deleteSyncPoint(createdSyncPoint.Id);
        } else {
            System.out.println("No SyncPoint was created. Nothing to delete.");
        }
        System.out.println("Finished SyncPoint deleting.");
    }

    private static void createFolder() {
        System.out.println("Starting Folder creation..");
        if (createdSyncPoint == null) {
            System.err.println("No SyncPoint was created. No Folder will be created.");
            return;
        }
        Folder folder = new Folder();
        folder.Name = "NewFolder";
        folder.Status = FolderStatus.Added;
        Folder[] folders = { folder };
        Folder[] createdFolders = FolderService.createFolders(createdSyncPoint.Id, createdSyncPoint.RootFolderId,
                folders);
        if (createdFolders == null || createdFolders.length == 0) {
            System.err.println("No folder was created.");
            return;
        }
        createdFolder = createdFolders[0];
        System.out.println(String.format("Finished Folder creation. New Folder id: %s", createdFolder.FolderId));
    }

    private static void getFolder() {
        Folder folder = FolderService.getFolder(createdSyncPoint.Id, createdFolder.FolderId, true);
        if (folder == null) {
            System.err.println(String.format("Could not find folder with SyncPointId=%s and FolderId=%s",
                    createdSyncPoint.Id, createdFolder.FolderId));
        } else {
            System.out.println(String.format("Retrieved folder with SyncPointId=%s and FolderId=%s",
                    createdSyncPoint.Id, createdFolder.FolderId));
        }
    }

    private static void deleteFolder() {
        System.out.println("Starting Folder deleting..");
        if (createdFolder != null) {
            FolderService.deleteFolder(createdSyncPoint.Id, createdFolder.FolderId);
        } else {
            System.out.println("No Folder was created. Nothing to delete.");
        }
        System.out.println("Finished Folder deleting.");
    }

    private static void uploadFile() {
        System.out.println("Starting File upload..");
        if (createdSyncPoint == null) {
            System.err.println("The syncpoint was not created at previous steps. No File will be uploaded.");
            return;
        }
        Folder folder = FolderService.getFolder(createdSyncPoint.Id, createdSyncPoint.RootFolderId, true);
        String storageEnpointId = createdSyncPoint.StorageEndpointId;
        StorageEndpoint[] storageEndpoints = StorageEndpointService.getStorageEndpoints(true);
        StorageEndpoint storageEndpoint = null;
        for (StorageEndpoint endpoint : storageEndpoints) {
            if (endpoint.Id.equals(storageEnpointId)) {
                storageEndpoint = endpoint;
            }
        }
        System.out.println(String.format("Using storage endpoint %s - %s", storageEndpoint.Id, storageEndpoint.Name));

        try {
            byte[] fileBody = "file body".getBytes();
            String fileName = "newFile.txt";

                        String path = ConfigurationHelper.getUploadFilePath();
            if (StringUtils.isNotEmpty(path)) {
                fileBody = Files.readAllBytes(Paths.get(path));
                fileName = new java.io.File(path).getName();
            }

            String result = FileService.uploadFile(storageEndpoint.Urls[0].Url, folder.VirtualPath, fileName,
                    folder.SyncpointId, fileBody);
            System.out.println(String.format("Finished File upload. File upload result: %s", result));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getFile() {
        System.out.println("Retrieving the 1st file in the created syncpoint..");
        SyncPoint syncPoint = createdSyncPoint;
        if (syncPoint == null) {
            System.err.println("The syncpoint was not created at previous steps. No file will be retrieved.");
            return;
        }
        Folder folder = FolderService.getFolder(syncPoint.Id, syncPoint.RootFolderId, true);
        File[] files = folder.Files;
        if (files.length == 0) {
            System.out.println("No files in the syncpoint.");
            return;
        }
        long fileId = files[0].FileId;
        File file = FileService.getFile(syncPoint.Id, fileId, true);
        if (file == null) {
            System.err.println(
                    String.format("Could not find file with SyncPointId=%s and FileId=%d", syncPoint.Id, fileId));
            return;
        }
        uploadedFile = file;
        System.out.println(String.format("Retrieved file with SyncPointId=%s and FileId=%s. Filename is %s.",
                syncPoint.Id, fileId, file.Filename));
    }

    private static void downloadFile() {
        System.out.println(String.format("Downloading the file %s..", uploadedFile.Filename));
        String downloadedFile = FileService.downloadFile(createdSyncPoint.Id, uploadedFile.FileId, true);
        if (downloadedFile == null) {
            System.err.println("Could not download the file.");
        } else {
            System.out.println(String.format("Successfully downloaded the file. File size : %s Bytes.",
                    downloadedFile.getBytes().length));
            System.out.println(downloadedFile);
        }
        System.out.println(String.format("Finished downloading the file %s.", uploadedFile.Filename));
    }

    private static void generateLink() {
        System.out.println("Generating share link for the uploaded file..");
        if (uploadedFile == null) {
            System.err.println("No files were uploaded in the new syncpoint. The link will not be generated.");
        }
        Link link = new Link();
        link.SyncPointId = uploadedFile.SyncpointId;
        link.VirtualPath = uploadedFile.VirtualPath + uploadedFile.Filename;
        link.LinkExpireInDays = 2;
        link.LinkExpirationPolicy = 1; // Enabled
        link.ShareLinkPolicy = 3; // Allow all
        link.PasswordProtectPolicy = 1; // Disabled
        Link[] links = { link };
        Link[] generatedLinks = LinkService.generateLinks(links);
        if (generatedLinks.length == 0) {
            System.err.println("No links were generated.");
            return;
        }
        generatedLink = generatedLinks[0];
        System.out.println(String.format("Generated a link with URL = %s, token = %s.", generatedLink.DownloadUrl,
                generatedLink.Token));
    }

    private static void getLink() {
        System.out.println(String.format("Retrieving a share link with token = %s...", generatedLink.Token));
        Link retrievedLink = LinkService.getLink(generatedLink.Token, true);
        if (retrievedLink == null) {
            System.err.println(String.format("Could not retrieve a share link with token = %s.", generatedLink.Token));
            return;
        }
        System.out.println(String.format("Retrieved a share link with token = %s.", generatedLink.Token));
    }

    private static void deleteLink() {
        System.out.println(String.format("Deleting a share link with token = %s...", generatedLink.Token));
        LinkService.deleteLink(generatedLink.Token);
        System.out.println(String.format("Deleted a share link with token = %s.", generatedLink.Token));
    }
}
