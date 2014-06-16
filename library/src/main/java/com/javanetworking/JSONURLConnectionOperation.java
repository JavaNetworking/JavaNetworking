package com.javanetworking;

import java.net.HttpURLConnection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 {@link JSONURLConnectionOperation} is a {@link HttpURLConnectionOperation} subclass for downloading JSON content.
 
 By default {@link JSONURLConnectionOperation} accepts the following MIME content types:
 
 - `application/json`
 - `text/json`
 - `text/javascript`
 */
public class JSONURLConnectionOperation extends HttpURLConnectionOperation {

	/**
	 A static constructor method that creates and returns a {@link JSONURLConnectionOperation} instance.
	 */
	public static JSONURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, HttpCompletion completion) {
		return new JSONURLConnectionOperation(urlConnection, completion);
	}

    /**
     A static constructor method that creates and returns a {@link JSONURLConnectionOperation} instance.
     */
    public static JSONURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, String requestBody, HttpCompletion completion) {
        return new JSONURLConnectionOperation(urlConnection, requestBody, completion);
    }

	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link JSONCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link JSONCompletion} instance that handles the completion interface methods.
	 */
	public JSONURLConnectionOperation(HttpURLConnection urlConnection, HttpCompletion completion) {
		this(urlConnection, null, completion);
	}

    /**
     Instantiate this class and sets the {@link HttpURLConnection}, and the {@link JSONCompletion} interface.

     This is the preferred constructor.

     @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
     @param requestBody A string representation of POST/PUT HTTP request body.
     @param completion A {@link JSONCompletion} instance that handles the completion interface methods.
     */
    public JSONURLConnectionOperation(HttpURLConnection urlConnection, String requestBody, HttpCompletion completion) {
        super(urlConnection, requestBody, null);

        this.setCompletion(completion);
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
	 Sets the {@link HttpCompletion} interface that responds to this operation.
	 
	 Parses the response to {@link JsonElement} object.
	 */
	@Override
	protected void setCompletion(final HttpCompletion completion) {
		super.setCompletion(new HttpCompletion() {
			@Override
			public void failure(HttpURLConnection httpConnection, Throwable t) {
				if (completion != null) {
					completion.failure(httpConnection, t);
				}
			}
			@Override
			public void success(HttpURLConnection httpConnection, Object response) {
				if (completion != null) {
					
					JsonElement jsonElement = new Gson().fromJson(new String((byte[])response), JsonElement.class);
					
					completion.success(httpConnection, jsonElement);
				}
			}
		});
	}
}
