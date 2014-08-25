package com.javanetworking;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 {@link XMLURLRequestOperation} is a {@link HTTPURLRequestOperation} subclass for downloading XML content.
 
 By default {@link XMLURLRequestOperation} accepts the following MIME content types:
 
 - `application/xml`
 - `text/xml`
 */
public class XMLURLRequestOperation extends HTTPURLRequestOperation {
	
	/**
	 A static constructor method that creates and returns a {@link XMLURLRequestOperation} instance.
	 */
	public static XMLURLRequestOperation operationWithURLRequest(URLRequest request, HTTPCompletion completion) {
		return new XMLURLRequestOperation(request, completion);
	}

	/**
	 Instantiate this class and sets the {@link URLRequest}, and the {@link HTTPCompletion} interface.

	 This is the preferred constructor.

	 @param urlConnection An open {@link URLRequest} to be used for HTTP network access.
	 @param completion A {@link HTTPCompletion} instance that handles the completion interface methods.
	 */
	public XMLURLRequestOperation(URLRequest request, HTTPCompletion completion) {
		super(request, null);

		this.setCompletion(completion);
	}
	
	/**
	 Get acceptable content types list for current connection. Default values for {@link XMLURLRequestOperation} is:
	 
 	 - `application/xml`
 	 - `text/xml`
	 
	 @return A {@code List<String>} with acceptable content types.
	 */
	@Override
	protected List<String> getAcceptableContentTypes() {
		return HTTPURLRequestOperation.arrayToList(new String[] { "application/xml", "text/xml" });
	}
	
	/**
	 Sets the {@link HTTPCompletion} interface that responds to this operation.
	 
	 Parses the response data to {@link Document} object.
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
			public void success(URLRequest request, Object responseData) {
				if (completion != null) {
					
					String xmlContent = new String((byte[])responseData);
					
					Document document = null;
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						document = builder.parse(new InputSource(new StringReader(xmlContent)));
						document.getDocumentElement().normalize();

						if (completion != null) {
							completion.success(request, document);
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
