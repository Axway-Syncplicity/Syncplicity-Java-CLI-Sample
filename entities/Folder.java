package entities;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Folder implements Serializable {
	public long SyncpointId;

	public long FolderId;

	public String VirtualPath;

	public String Name;

	public FolderStatus Status = FolderStatus.values()[0];

	public File[] Files;

	public Folder[] Folders;

	/** 
	 * Returns a logging-friendly string
	 */
	public final String ToLoggingString() {
		return String.format("Virtual Path: %1$s, Name: %2$s, Status: %3$s, VirtualFolderId: %4$s, DataFolderId: %5$s",
				VirtualPath, Name, Status.toString(), SyncpointId, FolderId);
	}
}