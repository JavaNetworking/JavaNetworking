package com.javanetworking;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 {@link XMLURLConnectionOperation} is a {@link HttpURLConnectionOperation} subclass for downloading XML content.
 
 By default {@link XMLURLConnectionOperation} accepts the following MIME content types:
 
 - `application/xml`
 - `text/xml`
 */
public class XMLURLConnectionOperation extends HttpURLConnectionOperation {
	
	/**
	 A static constructor method that creates and returns a {@link XMLURLConnectionOperation} instance.
	 */
	public static XMLURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, HttpCompletion completion) {
		return new XMLURLConnectionOperation(urlConnection, completion);
	}

	/**
	 A static constructor method that creates and returns a {@link XMLURLConnectionOperation} instance.
	 */
	public static XMLURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, String requestBody, HttpCompletion completion) {
		return new XMLURLConnectionOperation(urlConnection, requestBody, completion);
	}

	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link XMLCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link XMLCompletion} instance that handles the completion interface methods.
	 */
	public XMLURLConnectionOperation(HttpURLConnection urlConnection, HttpCompletion completion) {
		this(urlConnection, null, completion);
	}

	/**
	 Instantiate this class and sets the {@link HttpURLConnection}, and the {@link XMLCompletion} interface.

	 This is the preferred constructor.

	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param requestBody A string representation of POST/PUT HTTP request body.
	 @param completion A {@link XMLCompletion} instance that handles the completion interface methods.
	 */
	public XMLURLConnectionOperation(HttpURLConnection urlConnection, String requestBody, HttpCompletion completion) {
		super(urlConnection, requestBody, null);

		this.setCompletion(completion);
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
	 Sets the {@link HttpCompletion} interface that responds to this operation.
	 
	 Parses the response data to {@link Document} object.
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
					
					String xmlContent = new String((byte[])responseData);
					
					Document document = null;
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						document = builder.parse(new InputSource(new StringReader(xmlContent)));

						if (completion != null) {
							completion.success(httpConnection, document);
						}
					} catch (Exception e) {
						if (completion != null) {
							completion.failure(httpConnection, e);
						}
					}
				}
			}
		});
	}
}
