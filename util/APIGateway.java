package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import oauth.OAuth;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

public abstract class APIGateway {
	
	private final static String ACCEPT_HEADER = "Accept";
	private final static String JSON_CONTENT_TYPE = "application/json";

	protected static String provisioningAPIUrlPrefix;

	static {
		provisioningAPIUrlPrefix = ConfigurationHelper.getBaseApiEndpointUrl() + "provisioning/";
	}

	/**
	 * Creates request object to invoke the api REST call.
	 *
	 * @param method The request's method.
	 * @param uri The url of request.
	 *
	 * @return Created request.
	 */
	private static HttpURLConnection createRequest(
            String method,
            String uri,
            Map<String, String> additionalHeaders,
            boolean isAuthenticationCall,
            boolean isMachineAuthCall
    ) throws IOException {
		return createRequest(
				method,
				uri,
				additionalHeaders,
				isAuthenticationCall,
				isMachineAuthCall,
				false);
	}

	/**
	 * Creates request object to invoke the api REST call.
	 * 
	 * @param method The request's method.
	 * @param uri The url of request.
	 * 
	 * @return Created request.
	 */
	private static HttpURLConnection createRequest(
			String method,
			String uri,
			Map<String, String> additionalHeaders,
			boolean isAuthenticationCall,
			boolean isMachineAuthCall,
			boolean useMachineAccessTokenInsteadOfUserAccessToken
	) throws IOException {
		
		System.out.println(String.format("Creating %s request to %s", method.toUpperCase(), uri));

		URL url = new URL(uri);

		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.setRequestMethod(method.toUpperCase());
		request.setRequestProperty(ACCEPT_HEADER, JSON_CONTENT_TYPE);
		request.setConnectTimeout(15000);
		request.setDoOutput(true);
		request.setDoInput(true);

		if(additionalHeaders != null) {
			for (Map.Entry<String, String> entry: additionalHeaders.entrySet()) {
				request.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}

		return applyConsumerCredentials(
				request,
				isAuthenticationCall,
				isMachineAuthCall,
				useMachineAccessTokenInsteadOfUserAccessToken );
	}

	/**
	 * Writes the body to the request.
	 * 
	 * @param request The request object.
	 * @param body The string representation of body.
	 */
	private static void writeBody(HttpURLConnection request, String body, String contentType)
			throws IOException {
		

		if( contentType.equals( "application/json") ) {
			//This is just to pretty-print the JSON response to the console, you do not
			//need to do this of course for the real application that you would write.
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(body);
			body = gson.toJson(je);
			body = body.replaceAll(" ", "  " );
		}
		
		System.out.println( "[Body] " + body);

		OutputStream requestStream = request.getOutputStream();

		requestStream.write(body.getBytes(Charset.forName("UTF-8")));
		requestStream.flush();
		requestStream.close();
	}

	/**
	 * Writes the body to the request.
	 *
	 * @param request The request object.
	 * @param body The string representation of body.
	 */
	private static void writeBody(HttpURLConnection request, byte[] body, String contentType)
			throws IOException {
		System.out.println( "[Body] " + body);

		OutputStream requestStream = request.getOutputStream();

		requestStream.write(body);
		requestStream.flush();
		requestStream.close();
	}

	/**
	 * Applies the application key and secret to the request
	 * 
	 * @param request The request object.
	 * 
	 * @return The current request.
	 */
	private static HttpURLConnection applyConsumerCredentials(
			HttpURLConnection request,
			boolean isAuthenticationCall,
			boolean isMachineAuthCall,
			boolean useMachineAccessTokenInsteadOfUserAccessToken
	) {

		//If this is the first OAuth authentication call (to oauth/token endpoint),
		// then we don't have an OAuth Bearer token (access token), so we will use the
		//Application Key and Application Secret as the consumer credentials for the application.  However, once we've successfully
		//connected to the api gateway for the first time, we will receive an OAuth access token (Bearer token), you will
		//need to manage that bearer token and use it for subsequent calls to the API gateway.

		String appKey = ConfigurationHelper.getApplicationKey();
		if( isAuthenticationCall ) {
			String appSecret = ConfigurationHelper.getApplicationSecret();

			String encoded = Base64.getEncoder().encodeToString((appKey + ":" + appSecret).getBytes());
			System.out.println("[Header] Authorization: Basic " + encoded + "\n"
					+ "\t\t(Base64 encoded combination of App key and App secret)\n"
					+ "\t\t" + appKey + ":" + appSecret);
			request.addRequestProperty("Authorization", "Basic " + encoded);

			if (isMachineAuthCall) {
				// handling the call to oauth/token for obtaining Machine access token for SVA case
				String machineToken = ConfigurationHelper.getMachineToken();

				System.out.println("[Header] Sync-Machine-Token: " + machineToken);
				request.setRequestProperty("Sync-Machine-Token", machineToken);
			} else {
				// regular call to oauth/token for obtaining user access token for most of the cases
				String userToken = ConfigurationHelper.getSyncplicityAdminKey();

				System.out.println("[Header] Sync-App-Token: " + userToken);
				request.setRequestProperty("Sync-App-Token", userToken);
			}
		}
		else {
			System.out.println( "[Header] AppKey: " + appKey);
			request.setRequestProperty("AppKey", appKey);

			String accessToken = useMachineAccessTokenInsteadOfUserAccessToken ?
					APIContext.getMachineAccessToken() :
					APIContext.getAccessToken();
			System.out.println( "[Header] Authorization: Bearer " + accessToken);
			request.setRequestProperty("Authorization", "Bearer " + accessToken);
		}

		return request;
	}

	/**
	 * Reads the response from the request and returns the received object.
	 * 
	 * @param request The request object.
	 * @param classType The type of received object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	private static <T> T readResponse(HttpURLConnection request, Class<T> classType, boolean suppressErrors) {
        return readResponse(request, classType, suppressErrors, null);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T readResponse(HttpURLConnection request, Class<T> classType, boolean suppressErrors, BooleanResult shouldRefreshToken ) {

		try {
			if( shouldRefreshToken != null ) {
				shouldRefreshToken.setResult(false);
			}
			
			System.out.println();
			System.out.println("Trying to read response...");

			InputStream responseStream = request.getInputStream();

			if (responseStream == null) {
				System.out.println("Response wasn't received.");
				return null;
			}

			String response;
			BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));

			String line;
			StringBuilder responseBuffer = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				responseBuffer.append(line);
				responseBuffer.append('\r');
			}
			reader.close();
			response = responseBuffer.toString();

			if( StringUtils.isEmpty(response) || StringUtils.isWhitespace(response) ) {
				System.out.println("Received response is empty.");
				return null;
			}

			//This is just to pretty-print the JSON response to the console, you do not
			//need to do this of course for the real application that you would write.
			try {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(response);
				String prettyJsonString = gson.toJson(je);
				prettyJsonString = prettyJsonString.replaceAll(" ", "  ");

				System.out.println("Response: \n" + prettyJsonString);

				if (!classType.isAssignableFrom(String.class)) {
					return JSONSerialization.deserialize(response, classType);
				}
			} catch (Exception e) {
				System.out.println("Could not parse the response as JSON. Probably the response is of some other format.");
			}

			return (T) response;

		} 
		catch (IOException e) {
			
			if( !suppressErrors ) {
				System.err.println();
				System.err.println(String.format("\tError occurs during request to %s.", request.getURL().toString()));
				
				try {
					System.err.println(String.format("\tReceived: %d %s.", request.getResponseCode(), request.getResponseMessage()));
				} catch (IOException e1) { }
				
				e.printStackTrace();
			}
			
			try {
				// it's needed to authorize again and then send the same request again
                if ( shouldRefreshToken != null &&
                    (request.getResponseCode() == 401 ||
                    (request.getResponseCode() == 403 && request.getResponseMessage() == "Forbidden")) )
                {
                    shouldRefreshToken.setResult(true);
                }
			} catch (IOException e1) { }
		}

		return null;
	}


	/**
	 * Create GET HTTP request to url and return deserialized object of type
	 * type.
	 * 
	 * @param uri       The request url.
	 * @param classType The type of returned object.
	 * @
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpGet(String uri, Class<T> classType) {
		return httpGet( uri, classType, false );
	}

	/**
	 * Create GET HTTP request to url and return deserialized object of type
	 * type.
	 *
	 * @param uri             The request url.
	 * @param classType      The type of returned object.
	 * @param suppressErrors boolean to determine if output should be print to console on errors
	 *
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpGet(String uri, Class<T> classType, boolean suppressErrors) {
		return httpGet(uri, null, classType, suppressErrors);
	}

	/**
	 * Create GET HTTP request to url and return deserialized object of type
	 * type.
	 *
	 * @param uri             The request url.
	 * @param classType      The type of returned object.
	 * @param suppressErrors boolean to determine if output should be print to console on errors
	 *
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpGet(
            String uri,
            Map<String, String> additionalHeaders,
            Class<T> classType,
            boolean suppressErrors) {
		return httpGet(
				uri,
				additionalHeaders,
				classType,
				suppressErrors,
				false);
	}

	/**
	 * Create GET HTTP request to url and return deserialized object of type
	 * type.
	 * 
	 * @param uri             The request url.
	 * @param classType      The type of returned object.
	 * @param suppressErrors boolean to determine if output should be print to console on errors
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpGet(
			String uri,
			Map<String, String> additionalHeaders,
			Class<T> classType,
			boolean suppressErrors,
			boolean useMachineAccessTokenInsteadOfUserAccessToken) {
		HttpURLConnection request;
		String method = "GET";
		try {
			request = createRequest(
					method,
					uri,
					additionalHeaders,
					false,
					false,
					useMachineAccessTokenInsteadOfUserAccessToken);
		} catch (IOException e) {
			e.printStackTrace();

			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, suppressErrors, shouldRefreshToken);

        if (shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, GET request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send GET request again for the last time.");

            try {
    			request = createRequest(method, uri, null, false, false);
    		} catch (IOException e) {
    			e.printStackTrace();
    			
    			return null;
    		}
            response = readResponse(request, classType, suppressErrors );
        }
        
		return response;
	}

	/**
	 * Create POST HTTP request to url with body and return deserialized object
	 * of type classType.
	 *
	 *
 * @param isAuthenticationCall
 * @param isMachineAuthCall
 * @param uri       The request url.
	 * @param contentType
 * @param body      The request body.
	 * @param classType The type of returned object.
		 *
		 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpPost(
			boolean isAuthenticationCall,
			boolean isMachineAuthCall,
			String uri,
			String contentType,
			String body,
			Class<T> classType
	) {
		return httpPost(
				isAuthenticationCall,
				isMachineAuthCall,
				false,
				uri,
				contentType,
				body,
				null,
				classType);
	}

	/**
	 * Create POST HTTP request to url with body and return deserialized object
	 * of type classType.
	 * 
	 * @param uri       The request url.
	 * @param body      The request body.
	 * @param classType The type of returned object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpPost(
			boolean isAuthenticationCall,
			boolean isMachineAuthCall,
			boolean useMachineAccessTokenInsteadOfUserAccessToken,
			String uri,
			String contentType,
			String body,
			Map<String, String> additionalHeaders,
			Class<T> classType
	) {
		HttpURLConnection request;
		String method = "POST";
		try {
			request = createRequest(
					method,
					uri,
					additionalHeaders,
					isAuthenticationCall,
					isMachineAuthCall,
					useMachineAccessTokenInsteadOfUserAccessToken );
			request.setRequestProperty("Content-Type", contentType );

			writeBody(request, body, contentType);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, false, shouldRefreshToken);
        
        if (!isAuthenticationCall && shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, POST request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send POST request again for the last time.");
            
            try {
            	request = createRequest(method, uri, additionalHeaders, false, false);
    			request.setRequestProperty("Content-Type", contentType );

    			writeBody(request, body, contentType);
    		} catch (IOException e) {
    			e.printStackTrace();  			
    			return null;
    		}
            
            response = readResponse(request, classType, false);
        }
        		
		return response;
	}

	/**
	 * Create POST HTTP request to url with body and return deserialized object
	 * of type classType.
	 *
	 * @param uri       The request url.
	 * @param body      The request body as byte[]
	 * @param classType The type of returned object.
	 *
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpPost(
			boolean isAuthenticationCall,
			boolean isMachineAuthCall,
			boolean useMachineAccessTokenInsteadOfUserAccessToken,
			String uri,
			String contentType,
			byte[] body,
			Map<String, String> additionalHeaders,
			Class<T> classType
	) {
		HttpURLConnection request;
		String method = "POST";
		try {
			request = createRequest(
					method,
					uri,
					additionalHeaders,
					isAuthenticationCall,
					isMachineAuthCall,
					useMachineAccessTokenInsteadOfUserAccessToken);
			request.setRequestProperty("Content-Type", contentType);

			writeBody(request, body, contentType);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
		T response = readResponse(request, classType, false, shouldRefreshToken);

		if (!isAuthenticationCall && shouldRefreshToken.getResult()) {
			System.out.println();
			System.out.println("Trying to re-authenticate using the same credentials.");

			// it's needed to authorize again
			// trying to do it and then re-send the initial request
			OAuth.refreshToken();

			System.out.println();
			if (!APIContext.isAuthenticated()) {
				System.out.println("The OAuth authentication has failed, POST request can't be performed.");
				return null;
			}

			System.out.println("Authentication was successful. Trying to send POST request again for the last time.");

			try {
				request = createRequest(method, uri, additionalHeaders, false, false);
				request.setRequestProperty("Content-Type", contentType);

				writeBody(request, body, contentType);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			response = readResponse(request, classType, false);
		}

		return response;
	}

	/**
	 * Create POST HTTP request to url with entity and return deserialized
	 * object of type type.
	 * 
	 * @param uri The request url.
	 * @param entity The entity.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T httpPost(String uri, String contentType, T entity ) {
		
		return httpPost(
				false,
				false,
				uri,
				contentType,
				JSONSerialization.serialize(entity),
				(Class<T>) entity.getClass()
		);
	}

	/**
	 * Create PUT HTTP request to url with body and return deserialized object
	 * of type classType.
	 * 
	 * @param uri The request url.
	 * @param body The request body.
	 * @param classType The type of returned object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpPut(String uri, String body, Class<T> classType) {
		HttpURLConnection request;
		String method = "PUT";
		try {
			request = createRequest(method, uri, null, false, false);
			request.setRequestProperty("Content-Type", JSON_CONTENT_TYPE);

			writeBody(request, body, JSON_CONTENT_TYPE);
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, false, shouldRefreshToken);
        
        if (shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, PUT request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send PUT request again for the last time.");
            
            try {
            	request = createRequest(method, uri, null, false, false);
            	request.setRequestProperty("Content-Type", JSON_CONTENT_TYPE);

            	writeBody(request, body, JSON_CONTENT_TYPE);
    		} catch (IOException e) {
    			
    			e.printStackTrace();  			
    			return null;
    		}
            
            response = readResponse(request, classType, false);
        }
        
        return response;
	}

	/**
	 * Create PUT HTTP request to url with entity and return deserialized object
	 * of type T.
	 * 
	 * @param uri The request url.
	 * @param entity The entity.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T httpPut(String uri, T entity) {
		
		return httpPut(uri, JSONSerialization.serialize(entity), (Class<T>) entity.getClass());
	}

	/**
	 * Create DELETE HTTP request to url with body and return deserialized
	 * object of type classType.
	 * 
	 * @param uri The request url.
	 * @param classType The type of returned object.
	 * 
	 * @return The object representation of received response or null if
	 *         response is empty.
	 */
	protected static <T> T httpDelete(String uri, Class<T> classType) {
		HttpURLConnection request;
		String method = "DELETE";
		try {
			request = createRequest(method, uri, null, false, false);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		BooleanResult shouldRefreshToken = new BooleanResult();
        T response = readResponse(request, classType, false, shouldRefreshToken);
        
        if (shouldRefreshToken.getResult())
        {
        	System.out.println();
        	System.out.println("Trying to re-authenticate using the same credentials.");

            // it's needed to authorize again
            // trying to do it and then re-send the initial request
            OAuth.refreshToken();

            System.out.println();
            if (!APIContext.isAuthenticated())
            {
            	System.out.println("The OAuth authentication has failed, DELETE request can't be performed.");
                return null;
            }

            System.out.println("Authentication was successful. Trying to send DELETE request again for the last time.");
            
            try {
            	request = createRequest(method, uri, null, false, false);
    		} catch (IOException e) {
    			
    			e.printStackTrace();  			
    			return null;
    		}
            
            response = readResponse(request, classType, false);
        }
        
		return response;
	}
}
