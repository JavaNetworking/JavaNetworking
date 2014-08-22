package com.javanetworking;

import java.util.List;

import com.javanetworking.gson.Gson;
import com.javanetworking.gson.JsonElement;

/**
 {@link JSONURLRequestOperation} is a {@link HTTPURLRequestOperation} subclass for downloading JSON content.
 
 By default {@link JSONURLRequestOperation} accepts the following MIME content types:
 
 - `application/json`
 - `text/json`
 - `text/javascript`
 */
public class JSONURLRequestOperation extends HTTPURLRequestOperation {

	/**
	 A static constructor method that creates and returns a {@link JSONURLConnectionOperation} instance.
	 */
	public static JSONURLRequestOperation operationWithURLRequest(URLRequest request, HTTPCompletion completion) {
		return new JSONURLRequestOperation(request, completion);
	}

    /**
     Instantiate this class and sets the {@link URLRequest}, and the {@link HTTPCompletion} interface.

     This is the preferred constructor.

     @param urlConnection An open {@link URLRequest} to be used for HTTP network access.
     @param completion A {@link HTTPCompletion} instance that handles the completion interface methods.
     */
    public JSONURLRequestOperation(URLRequest request, HTTPCompletion completion) {
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
		return HTTPURLRequestOperation.arrayToList(new String[] { "application/json", "text/json", "text/javascript" });
	}
	
	/**
	 Sets the {@link HTTPCompletion} interface that responds to this operation.
	 
	 Parses the response to {@link JsonElement} object.
	 */
	@Override
	protected void setCompletion(final HTTPCompletion completion) {
		super.setCompletion(new HTTPCompletion() {
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
					} catch (Exception e) {
						if (completion != null) {
							completion.failure(request, e);
						}
					}
				}
			}
		});
	}
}
