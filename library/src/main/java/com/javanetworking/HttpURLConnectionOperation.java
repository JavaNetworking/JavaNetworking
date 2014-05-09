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

public class HttpURLConnectionOperation extends URLConnectionOperation {
	
	public interface HttpCompletion {
		void failure(HttpURLConnection httpConnection, Throwable t);
		void success(HttpURLConnection httpConnection, byte[] responseData);
	}
	
	public static HttpURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, HttpCompletion httpCompletion) {
		return (HttpURLConnectionOperation) new HttpURLConnectionOperation(urlConnection, httpCompletion);
	}

	private Error error;
	
	public List<Integer> acceptableResponseCodes;
	public List<String> acceptableContentTypes;
	
	
	public HttpURLConnectionOperation(HttpURLConnection urlConnection, final HttpCompletion completion) {
		super(urlConnection, null);
		
		this.setHttpCompletion(completion);
		
		this.acceptableResponseCodes = HttpURLConnectionOperation.range(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_MULT_CHOICE);
		
		this.error = null;
	}
	
	public static List<Integer> range(int start, int stop) {
		List<Integer> range = new ArrayList<Integer>(stop-start);
		
		for (int i=0; i< stop-start; i++) {
			range.add(start+i);
		}
		
		return range;
	}
	
	public Error getError() {
		
		if (!hasAcceptableStatusCode()) {
			List<Integer> codes = this.acceptableResponseCodes;
			this.error = new Error(String.format(Locale.getDefault(), "Expected response code in range %s, got %d", (String.format("[%d, %d]", codes.get(0), codes.get(codes.size()-1))), getResponseCode()));
		}
		
		if (!hasAcceptableContenType()) {
			this.error = new Error(String.format(Locale.getDefault(), "Expected content types %s, got %s", this.acceptableContentTypes, getContentType()));
		}
		
		return this.error;
	}
	
	protected void setHttpCompletion(HttpCompletion completion) {
		super.setURLCompletion(completionWithHttpCompletion(completion));
	}
	
	private HttpURLConnection getHttpURLConnection() {
		return (HttpURLConnection) getURLConnection();
	}
	
	private int getResponseCode() {
		try {
			return getHttpURLConnection().getResponseCode();
		} catch (IOException e) {
			return -1;
		}
	}
	
	private boolean hasAcceptableStatusCode() {
		
		if (this.acceptableResponseCodes.contains(getResponseCode())) {
			return true;
		}
		
		return false;
	}
	
	private String getContentType() {
		return getURLConnection().getContentType();
	}
	
	private boolean hasAcceptableContenType() {
		
		if (this.acceptableContentTypes != null) {
			if (this.acceptableContentTypes.contains(getContentType()) != true) {
				return false;
			}
		}
		
		return true;
	}
	
	
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
