package com.javanetworking;

import java.net.HttpURLConnection;
import java.util.List;


/**
 {@link ImageURLConnectionOperation} is a {@link HttpURLConnectionOperation} subclass for downloading images.
 
 By default {@link ImageURLConnectionOperation} accepts the following MIME content types:
 
 - `image/tiff`
 - `image/jpeg`
 - `image/gif`
 - `image/png`
 - `image/ico`
 - `image/x-icon` 
 - `image/bmp`
 - `image/x-bmp`
 - `image/x-xbitmap`
 - `image/x-win-bitmap`
 */
public class ImageURLConnectionOperation extends HttpURLConnectionOperation {

	/**
	 A static constructor method that creates and returns a {@link ImageURLConnectionOperation} instance.
	 */
	public static ImageURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, HttpCompletion completion) {
		return new ImageURLConnectionOperation(urlConnection, completion);
	}
	
	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link ImageCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link ImageCompletion} instance that handles the completion interface methods.
	 */
	public ImageURLConnectionOperation(HttpURLConnection urlConnection, HttpCompletion completion) {
		super(urlConnection, null);
		
		this.setCompletion(completion);
	}
	
	/**
	 Get acceptable content types list for current connection. Default values for {@link ImageURLConnectionOperation} is:
	 
	 - `image/tiff`
	 - `image/jpeg`
 	 - `image/gif`
 	 - `image/png`
 	 - `image/ico`
 	 - `image/x-icon` 
 	 - `image/bmp`
 	 - `image/x-bmp`
 	 - `image/x-xbitmap`
 	 - `image/x-win-bitmap`
	 
	 @return A {@code List<String>} with acceptable content types.
	 */
	@Override
	protected List<String> getAcceptableContentTypes() {
		return HttpURLConnectionOperation.arrayToList(new String[] { "image/tiff", "image/jpeg", "image/gif", "image/png", "image/ico", "image/x-icon", "image/bmp", "image/x-bmp", "image/x-xbitmap", "image/x-win-bitmap" });
	}
	
	/**
	 Sets the {@link HttpCompletion} interface that responds to this operation.
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
			public void success(HttpURLConnection httpConnection, Object responseData) {
				if (completion != null) {
					completion.success(httpConnection, responseData);
				}
			}
		});
	}
}
