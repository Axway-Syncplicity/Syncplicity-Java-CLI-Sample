package entities.policies;

import java.io.Serializable;

/**
 * An enumeration of possible shareable link policies
 */
public enum SharedFolderResharingPolicy implements Serializable {
	/**
	 * Default policy
	 */
	Unknown(0),
	/**
	 * Disable folder re-sharing for external Users. Folders can only be reshared
	 * by folks in the same company.
	 */
	DisallowAll(1),
	/**
	 * Folders can only be re-shared by external users who has Editor permissions
	 */
	ExternalEditorOnly(2),
	/**
	 * Allow re-sharing by anyone (Consumer behavior)
	 */
	AllowAll(3);

	private int intValue;
	private static java.util.HashMap<Integer, SharedFolderResharingPolicy> mappings;

	private static java.util.HashMap<Integer, SharedFolderResharingPolicy> getMappings() {
		if (mappings == null) {
			synchronized (SharedFolderResharingPolicy.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<>();
				}
			}
		}
		return mappings;
	}

	SharedFolderResharingPolicy(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static SharedFolderResharingPolicy forValue(int value) {
		return getMappings().get(value);
	}
}