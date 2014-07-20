package com.javanetworking;

import java.net.HttpURLConnection;
import java.util.List;

import com.javanetworking.gson.Gson;
import com.javanetworking.gson.JsonElement;
import com.javanetworking.gson.JsonSyntaxException;

/**
 {@link JSONURLRequestOperation} is a {@link HttpURLRequestOperation} subclass for downloading JSON content.
 
 By default {@link JSONURLRequestOperation} accepts the following MIME content types:
 
 - `application/json`
 - `text/json`
 - `text/javascript`
 */
public class JSONURLRequestOperation extends HttpURLRequestOperation {

	/**
	 A static constructor method that creates and returns a {@link JSONURLConnectionOperation} instance.
	 */
	public static JSONURLRequestOperation operationWithURLRequest(URLRequest request, HttpCompletion completion) {
		return new JSONURLRequestOperation(request, completion);
	}

    /**
     Instantiate this class and sets the {@link HttpURLConnection}, and the {@link JSONCompletion} interface.

     This is the preferred constructor.

     @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
     @param completion A {@link JSONCompletion} instance that handles the completion interface methods.
     */
    public JSONURLRequestOperation(URLRequest request, HttpCompletion completion) {
        super(request, null);

        this.setCompletion(completion);
    }
	
	/**
	 Get acceptable content types list for current connection. Default values for {@link JSONURLRequestOperation} is:
	 
 	 - `application/json`
 	 - `text/json`
 	 - `text/javascript`
	 
	 @return A {@code List<String>} with acceptable content types.
	 */
	@Override
	protected List<String> getAcceptableContentTypes() {
		return HttpURLRequestOperation.arrayToList(new String[] { "application/json", "text/json", "text/javascript" });
	}
	
	/**
	 Sets the {@link HttpCompletion} interface that responds to this operation.
	 
	 Parses the response to {@link JsonElement} object.
	 */
	@Override
	protected void setCompletion(final HttpCompletion completion) {
		super.setCompletion(new HttpCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				if (completion != null) {
					completion.failure(request, t);
				}
			}
			@Override
			public void success(URLRequest request, Object response) {
				if (completion != null) {
					
					try {
						JsonElement jsonElement = new Gson().fromJson(new String((byte[])response), JsonElement.class);

						if (completion != null) {
							completion.success(request, jsonElement);
						}
					} catch(JsonSyntaxException e) {
						if (completion != null) {
							completion.failure(request, e);
						}
					}
				}
			}
		});
	}
}
