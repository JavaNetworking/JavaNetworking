package com.javanetworking;

import java.net.HttpURLConnection;
import java.util.List;

import com.javanetworking.HttpURLConnectionOperation.HttpCompletion;

/**
 {@link JSONURLConnectionOperation} is a {@link HttpURLConnectionOperation} subclass for downloading JSON content.
 
 By default {@link JSONURLConnectionOperation} accepts the following MIME content types:
 
 - `application/json`
 - `text/json`
 - `text/javascript`
 */
public class JSONURLConnectionOperation extends HttpURLConnectionOperation {

	/**
	 {@link JSONCompletion} is {@link JSONURLConnectionOperation}s completion interface
	 which indicates if the {@link HttpURLConnection} failed or succeeded.
	 */
	public interface JSONCompletion {
		void failure(HttpURLConnection httpConnection, Throwable t);
		void success(HttpURLConnection httpConnection, String responseData);
	}
	
	/**
	 A static constructor method that creates and returns a {@link JSONURLConnectionOperation} instance.
	 */
	public static JSONURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, JSONCompletion completion) {
		return new JSONURLConnectionOperation(urlConnection, completion);
	}
	
	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link JSONCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link JSONCompletion} instance that handles the completion interface methods.
	 */
	public JSONURLConnectionOperation(HttpURLConnection urlConnection, JSONCompletion completion) {
		super(urlConnection, null);
		
		this.setJSONCompletion(completion);
	}
	
	/**
	 Get acceptable content types list for current connection. Default values for {@link JSONURLConnectionOperation} is:
	 
 	 - `application/json`
 	 - `text/json`
 	 - `text/javascript`
	 
	 @return A {@code List<String>} with acceptable content types.
	 */
	@Override
	protected List<String> getAcceptableContentTypes() {
		return HttpURLConnectionOperation.arrayToList(new String[] { "application/json", "text/json", "text/javascript" });
	}
	
	/**
	 Sets the {@link JSONCompletion} interface that responds to this operation.
	 */
	protected void setJSONCompletion(JSONCompletion completion) {
		super.setHttpCompletion(completionWithJSONCompletion(completion));
	}
	
	/**
	 Creates a {@link HttpCompletion} interface mapped to an {@link JSONCompletion} interface.
	 
	 Before the {@link JSONCompletion} interface returns on a {@link HttpCompletion} success the
	 {@code getError()} method is called to verify HTTP response code and content type.
	 
	 @return A {@link HttpCompletion} instance mapped to a {@link JSONCompletion} interface.
	 */
	private HttpCompletion completionWithJSONCompletion(final JSONCompletion completion) {
		return new HttpCompletion() {
			@Override
			public void failure(HttpURLConnection httpConnection, Throwable t) {
				if (completion != null) {
					completion.failure(httpConnection, t);
				}
			}
			@Override
			public void success(HttpURLConnection httpConnection, byte[] responseData) {
				if (completion != null) {
					completion.success(httpConnection, new String(responseData));
				}
			}
		};
	}
}
