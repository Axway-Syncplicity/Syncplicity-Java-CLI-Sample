package entities.policies;

import java.io.Serializable;

public enum RestrictWebsiteAccessPolicy implements Serializable {
	/**
	 * Default value
	 */
	Unknown(0),
	/**
	 * Restriction policy is set off
	 */
	Disabled(1),
	/**
	 * Restriction policy is enabled
	 */
	Enabled(2);

	private int intValue;
	private static java.util.HashMap<Integer, RestrictWebsiteAccessPolicy> mappings;

	private static java.util.HashMap<Integer, RestrictWebsiteAccessPolicy> getMappings() {
		if (mappings == null) {
			synchronized (RestrictWebsiteAccessPolicy.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<>();
				}
			}
		}
		return mappings;
	}

	RestrictWebsiteAccessPolicy(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static RestrictWebsiteAccessPolicy forValue(int value) {
		return getMappings().get(value);
	}
}