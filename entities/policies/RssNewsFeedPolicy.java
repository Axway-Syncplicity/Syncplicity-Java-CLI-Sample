package entities.policies;

import java.io.Serializable;

public enum RssNewsFeedPolicy implements Serializable {
	/**
	 * Default value
	 */
	Unknown(0),
	/**
	 * Company users will not be able to subscribe to any RSS feeds
	 */
	Disabled(1),
	/**
	 * Company users will be able to subscribe to any RSS feeds
	 */
	Enabled(2);

	private int intValue;
	private static java.util.HashMap<Integer, RssNewsFeedPolicy> mappings;

	private static java.util.HashMap<Integer, RssNewsFeedPolicy> getMappings() {
		if (mappings == null) {
			synchronized (RssNewsFeedPolicy.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<>();
				}
			}
		}
		return mappings;
	}

	RssNewsFeedPolicy(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static RssNewsFeedPolicy forValue(int value) {
		return getMappings().get(value);
	}
}