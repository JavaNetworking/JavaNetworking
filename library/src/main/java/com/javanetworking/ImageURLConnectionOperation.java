package com.javanetworking;

import java.net.HttpURLConnection;
import java.util.List;

import com.javanetworking.HttpURLConnectionOperation.HttpCompletion;

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
	 {@link ImageCompletion} is {@link ImageURLConnectionOperation}s completion interface
	 which indicates if the {@link HttpURLConnection} failed or succeeded.
	 */
	public interface ImageCompletion {
		void failure(HttpURLConnection httpConnection, Throwable t);
		void success(HttpURLConnection httpConnection, byte[] responseData);
	}
	
	/**
	 A static constructor method that creates and returns a {@link ImageURLConnectionOperation} instance.
	 */
	public static ImageURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, ImageCompletion completion) {
		return new ImageURLConnectionOperation(urlConnection, completion);
	}
	
	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link ImageCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link ImageCompletion} instance that handles the completion interface methods.
	 */
	public ImageURLConnectionOperation(HttpURLConnection urlConnection, ImageCompletion completion) {
		super(urlConnection, null);
		
		this.setImageCompletion(completion);
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
	 Sets the {@link ImageCompletion} interface that responds to this operation.
	 */
	protected void setImageCompletion(ImageCompletion completion) {
		super.setHttpCompletion(completionWithImageCompletion(completion));
	}
	
	/**
	 Creates a {@link HttpCompletion} interface mapped to an {@link ImageCompletion} interface.
	 
	 Before the {@link ImageCompletion} interface returns on a {@link HttpCompletion} success the
	 {@code getError()} method is called to verify HTTP response code and content type.
	 
	 @return A {@link HttpCompletion} instance mapped to a {@link ImageCompletion} interface.
	 */
	private HttpCompletion completionWithImageCompletion(final ImageCompletion completion) {
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
					Error error = getError();
					if (error != null) {
						completion.failure(httpConnection, error);
					} else {
						completion.success(httpConnection, responseData);
					}
				}
			}
		};
	}
}
