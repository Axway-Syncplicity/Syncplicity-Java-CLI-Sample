package util;

import oauth.TokenResponse;

public class APIContext {
	
	private static TokenResponse tokenResponse   = null;
	private static boolean       hasStorageEndpoint = false;
	private static String        machineAccessToken = null;
	
	public static boolean isAuthenticated() {
		return (tokenResponse.getAccessToken() != null);
	}
	
	public static String getAccessToken() {
		return (tokenResponse != null ? tokenResponse.getAccessToken() : "");
	}
	
	public static String getRefreshToken() {
		return (tokenResponse != null ? tokenResponse.getRefreshToken() : "");
	}

	public static String getCompanyGuid ( ) {
		return (tokenResponse != null ? tokenResponse.getUserCompanyId() : "");
	}

	public static void setOAuthResponse( TokenResponse tokenResponseValue ) {
		tokenResponse = tokenResponseValue;
	}
	
	public static boolean hasStorageEndpoint() {
		return hasStorageEndpoint;
	}
	
	public static void setHasStorageEndpoint( boolean value ) {
		hasStorageEndpoint = value;
	}

	public static String getMachineAccessToken() { return machineAccessToken; }

	public static void setMachineAccessToken(String token) { machineAccessToken = token; }
}
