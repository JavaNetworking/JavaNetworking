package com.javanetworking;

import java.net.HttpURLConnection;
import java.util.List;

import com.javanetworking.HttpURLConnectionOperation.HttpCompletion;

/**
 {@link XMLURLConnectionOperation} is a {@link HttpURLConnectionOperation} subclass for downloading XML content.
 
 By default {@link XMLURLConnectionOperation} accepts the following MIME content types:
 
 - `application/xml`
 - `text/xml`
 */
public class XMLURLConnectionOperation extends HttpURLConnectionOperation {

	/**
	 {@link XMLCompletion} is {@link XMLURLConnectionOperation}s completion interface
	 which indicates if the {@link HttpURLConnection} failed or succeeded.
	 */
	public interface XMLCompletion {
		void failure(HttpURLConnection httpConnection, Throwable t);
		void success(HttpURLConnection httpConnection, String responseData);
	}
	
	/**
	 A static constructor method that creates and returns a {@link XMLURLConnectionOperation} instance.
	 */
	public static XMLURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, XMLCompletion completion) {
		return new XMLURLConnectionOperation(urlConnection, completion);
	}
	
	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link XMLCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link XMLCompletion} instance that handles the completion interface methods.
	 */
	public XMLURLConnectionOperation(HttpURLConnection urlConnection, XMLCompletion completion) {
		super(urlConnection, null);
		
		this.setXMLCompletion(completion);
	}
	
	/**
	 Get acceptable content types list for current connection. Default values for {@link XMLURLConnectionOperation} is:
	 
 	 - `application/xml`
 	 - `text/xml`
	 
	 @return A {@code List<String>} with acceptable content types.
	 */
	@Override
	protected List<String> getAcceptableContentTypes() {
		return HttpURLConnectionOperation.arrayToList(new String[] { "application/xml", "text/xml" });
	}
	
	/**
	 Sets the {@link XMLCompletion} interface that responds to this operation.
	 */
	protected void setXMLCompletion(XMLCompletion completion) {
		super.setHttpCompletion(completionWithXMLCompletion(completion));
	}
	
	/**
	 Creates a {@link HttpCompletion} interface mapped to an {@link XMLCompletion} interface.
	 
	 Before the {@link XMLCompletion} interface returns on a {@link HttpCompletion} success the
	 {@code getError()} method is called to verify HTTP response code and content type.
	 
	 @return A {@link HttpCompletion} instance mapped to a {@link XMLCompletion} interface.
	 */
	private HttpCompletion completionWithXMLCompletion(final XMLCompletion completion) {
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
