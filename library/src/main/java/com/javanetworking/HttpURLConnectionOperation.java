// Copyright (c) 2014 JavaNetworking (https://github.com/JavaNetworking/JavaNetworking)
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.javanetworking;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 {@link HttpURLConnectionOperation} is an extension of {@link URLConnectionOperation} and handles
 HTTP validation of response codes and content types.
 */
public class HttpURLConnectionOperation extends URLConnectionOperation {
	
	/**
	 {@link HttpCompletion} is {@link HttpURLConnectionOperation}s completion interface which indicates the
	 {@link URLConnection} failed or succeeded. 
	 */
	public interface HttpCompletion {
		void failure(HttpURLConnection httpConnection, Throwable t);
		void success(HttpURLConnection httpConnection, byte[] responseData);
	}
	
	/**
	 A static constructor method that creates and returns a {@link HttpURLConnectionOperation} instance.
	 */
	public static HttpURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, HttpCompletion httpCompletion) {
		return (HttpURLConnectionOperation) new HttpURLConnectionOperation(urlConnection, httpCompletion);
	}

	/**
	 The {@link Error} generated when response code or content type is unexpected values.
	 */
	private Error error;
	
	/**
	 The list of acceptable response codes. Default values are 200 to 299.
	 */
	public List<Integer> acceptableResponseCodes = null;
	
	/**
	 The list of acceptable content types. The list is empty by default.
	 */
	public List<String> acceptableContentTypes = null;
	
	
	/**
	 Instantiates this class and sets the {@link HttpURLConnection}, and the {@link HttpCompletion} interface.
	 
	 This is the preferred constructor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
	 @param completion A {@link HttpCompletion} instance that handles the completion interface methods.
	 */
	public HttpURLConnectionOperation(HttpURLConnection urlConnection, final HttpCompletion completion) {
		this(urlConnection, null, completion);
	}

    /**
     Instantiates this class and sets the {@link HttpURLConnection}, and the {@link HttpCompletion} interface.

     This is the preferred constructor.

     @param urlConnection An open {@link HttpURLConnection} to be used for HTTP network access.
     @param requestBody A string representation of POST/PUT HTTP request body.
     @param completion A {@link HttpCompletion} instance that handles the completion interface methods.
     */
    public HttpURLConnectionOperation(HttpURLConnection urlConnection, String requestBody, final HttpCompletion completion) {
        super(urlConnection, requestBody, null);

        this.setHttpCompletion(completion);

        addAcceptableResponseCodes(HttpURLConnectionOperation.range(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_MULT_CHOICE));

        this.error = null;
    }
	
	/**
	 Generates an integer list generator which is used to generate acceptable response codes.
	 
	 The list generated starts at the {@param start} value and stops at value {@param stop} - 1.
	 
	 @param start Start integer of the list of integers.
	 @param stop Stop integer, stops at value - 1. If 300 is given, stop value is 299.
	 */
	public static List<Integer> range(int start, int stop) {
		List<Integer> range = new ArrayList<Integer>(stop-start);
		
		for (int i=0; i< stop-start; i++) {
			range.add(start+i);
		}
		
		return range;
	}
	
	/**
	 Generate string list from strings array.
	 
	 Helper method to get content type list from {@code String[]}.
	 Example: {@code HttpURLConnectionOperation.arrayToList(new String[] { "application/text", "application/json", "application/xml" })}
	 
	 @param strings The string array to be placed into the {@link List<String>}. 
	 */
	public static List<String> arrayToList(String[] strings) {
		List<String> stringList = new ArrayList<String>();
		
		for (String string : strings) {
			stringList.add(string);
		}
		
		return stringList;
	}
	
	/**
	 Get acceptable response codes for current connection. Default values are set to 200 - 299.
	 
	 @return A {@link List<Integer>} which contains the acceptable response codes for current connection. 
	 */
	protected List<Integer> getAcceptableResponseCodes() {
		if (this.acceptableResponseCodes == null) {
			this.acceptableResponseCodes = new ArrayList<Integer>();
		}
		return this.acceptableResponseCodes;
	}
	
	/**
	 Add acceptable response codes to the existing list of accepted response codes.
	 
	 @param newResponseCodes A list of Integer values to be added to the list of already accepted response codes.
	 */
	protected void addAcceptableResponseCodes(List<Integer> newResponseCodes) {
		List<Integer> currentResponseCodes = getAcceptableResponseCodes();
		
		for (Integer responseCode : newResponseCodes) {
			currentResponseCodes.add(responseCode);
		}
	}
	
	/**
	 Get acceptable content types list for current connection. Default value is null.
	 
	 @return A {@link List<String>} which contains the acceptable content types for current connection.
	 */
	protected List<String> getAcceptableContentTypes() {
		if (this.acceptableContentTypes == null) {
			this.acceptableContentTypes = new ArrayList<String>();
		}
		return this.acceptableContentTypes;
	}
	
	/**
	 Add acceptable content types to the existing list of accepted content types.

	 @param newContentTypes A list of string values to be added to the list of acceptable content types.
	 */
	protected void addAcceptableContentTypes(List<String> newContentTypes) {
		List<String> currentContentTypes = getAcceptableContentTypes();

		for (String contentType : newContentTypes) {
			currentContentTypes.add(contentType);
		}
	}

	/**
	 Method called before {@link HttpCompletion} interface returns. Generates an {@link Error}
	 if unacceptable status code or unacceptable content type is detected. 
	 
	 @return An {@link Error} instance if an error is detected otherwise null is returned.
	 */
	public Error getError() {
		
		if (!hasAcceptableResponseCode()) {
			List<Integer> codes = getAcceptableResponseCodes();
			this.error = new Error(String.format(Locale.getDefault(), "Expected response code in range %s, got %d", (String.format("[%d, %d]", codes.get(0), codes.get(codes.size()-1))), getResponseCode()));
		}
		
		if (!hasAcceptableContenType()) {
			this.error = new Error(String.format(Locale.getDefault(), "Expected content types %s, got %s", getAcceptableContentTypes(), getContentType()));
		}
		
		return this.error;
	}
	
	/**
	 Sets the {@link HttpCompletion} interface that responds to this operation.
	 */
	protected void setHttpCompletion(HttpCompletion completion) {
		super.setURLCompletion(completionWithHttpCompletion(completion));
	}
	
	/**
	 Returns the {@link HttpURLConnection} used.
	 
	 @return The {@link HttpURLConnection} of this operation.
	 */
	private HttpURLConnection getHttpURLConnection() {
		return (HttpURLConnection) getURLConnection();
	}
	
	/**
	 Returns this {@link HttpURLConnection}s responseCode.
	 
	 @return An integer value indications this connections response code.
	 */
	private int getResponseCode() {
		try {
			return getHttpURLConnection().getResponseCode();
		} catch (IOException e) {
			return -1;
		}
	}
	
	/**
	 Validates that the current connection has acceptable response code by checking
	 if the acceptableResponseCodes list contains the current connections response code.
	 
	 @return A boolean value indicating if the current connection has acceptable response code.
	 */
	private boolean hasAcceptableResponseCode() {
		
		if (getAcceptableResponseCodes() != null) {
			if (getAcceptableResponseCodes().contains(getResponseCode())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 Gets the connections HTTP content type.
	 
	 @return A string value indicating the connections HTTP content type.
	 */
	private String getContentType() {
		return getURLConnection().getContentType();
	}
	
	/**
	 Validates that the current connection has acceptable content type by checking
	 if the acceptableContentTypes list contains the current connections content type.
	 
	 @return A boolean value indicating if the current connection has acceptable content type.
	 */
	private boolean hasAcceptableContenType() {
		
		List<String> contentTypes = getAcceptableContentTypes();
		if (contentTypes != null) {

			String currentContentType = getContentType();

			for (String contentType : contentTypes) {
				if (currentContentType.contains(contentType)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 Creates a {@link URLCompletion} interface mapped to a {@link HttpCompletion} interface.
	 
	 Before the {@link HttpCompletion} interface returns on a {@link URLCompletion} success the
	 {@code getError()} method is called to verify HTTP response code and content type.  
	 
	 @return A {@link URLCompletion} instance mapped to a {@link HttpCompletion} interface.
	 */
	private URLCompletion completionWithHttpCompletion(final HttpCompletion completion) {
		return new URLCompletion() {
			@Override
			public void failure(URLConnection urlConnection, Throwable t) {
				if (completion != null) {
					completion.failure((HttpURLConnection)urlConnection, t);
				}
			}
			
			@Override
			public void success(URLConnection urlConnection, byte[] responseData) {
				if (completion != null) {
					Error error = getError();
					if (error != null) {
						completion.failure((HttpURLConnection)urlConnection, error);
					} else {
						completion.success((HttpURLConnection)urlConnection, responseData);
					}
				}
			}
		};
	}
}
