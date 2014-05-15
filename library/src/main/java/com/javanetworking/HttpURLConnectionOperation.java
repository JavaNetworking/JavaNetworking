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
	 The {@link Error} generated when response code or content type is unexspected values.
	 */
	private Error error;
	
	/**
	 The list of acceptable response codes. Default values are 200 to 299.
	 */
	public List<Integer> acceptableResponseCodes;
	
	/**
	 The list of acceptable content types. The list is empty by default.
	 */
	public List<String> acceptableContentTypes;
	
	
	/**
	 Instansiates this class and sets the {@link HttpURLConnection}, and the {@link HttpCompletion} interface.
	 
	 This is the preferred construtor.
	 
	 @param urlConnection An open {@link HttpURLConnection} to be used for http network access.
	 @param completion A {@link HttpCompletion} instance that handles the completion interface methods.
	 */
	public HttpURLConnectionOperation(HttpURLConnection urlConnection, final HttpCompletion completion) {
		super(urlConnection, null);
		
		this.setHttpCompletion(completion);
		
		this.acceptableResponseCodes = HttpURLConnectionOperation.range(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_MULT_CHOICE);
		
		this.error = null;
	}
	
	/**
	 An integer list generator which is used to generate acceptable response codes.
	 
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
	 Method called before {@link HttpCompletion} interface returns. Generates an {@link Error}
	 if unacceptable status code or unacceptable content type is detected. 
	 
	 @return An {@link Error} instance if an error is detected otherwise null is returned.
	 */
	public Error getError() {
		
		if (!hasAcceptableResponseCode()) {
			List<Integer> codes = this.acceptableResponseCodes;
			this.error = new Error(String.format(Locale.getDefault(), "Expected response code in range %s, got %d", (String.format("[%d, %d]", codes.get(0), codes.get(codes.size()-1))), getResponseCode()));
		}
		
		if (!hasAcceptableContenType()) {
			this.error = new Error(String.format(Locale.getDefault(), "Expected content types %s, got %s", this.acceptableContentTypes, getContentType()));
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
		
		if (this.acceptableResponseCodes.contains(getResponseCode())) {
			return true;
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
		
		if (this.acceptableContentTypes != null) {
			if (this.acceptableContentTypes.contains(getContentType()) != true) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 Creates a {@link URLCompletion} from a {@link HttpCompletion}.
	 
	 @return A {@link URLCompletion} instance.
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
