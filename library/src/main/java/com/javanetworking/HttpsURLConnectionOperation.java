package com.javanetworking;

import java.io.IOException;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;


public class HttpsURLConnectionOperation extends HttpURLConnectionOperation {
	
	public static HttpsURLConnectionOperation operationWithHttpsURLConnection(HttpsURLConnection urlConnection, HttpCompletion httpsCompletion) {
		return new HttpsURLConnectionOperation(urlConnection, httpsCompletion);
	}
	
	HttpsURLConnection urlConnection;
	
	public HttpsURLConnectionOperation(HttpsURLConnection urlConnection, HttpCompletion completion) {
		super(urlConnection, null);
		
		this.urlConnection = urlConnection;
		
		super.setCompletion(completion);
	}
	
	public HttpsURLConnection getHttpsURLConnection() {
		return this.urlConnection;
	}
	
	@Override
	protected void setCompletion(final HttpCompletion completion) {
		super.setCompletion(new HttpCompletion() {
			@Override
			public void failure(HttpURLConnection urlConnection, Throwable t) {
				if (completion != null) {
					completion.failure((HttpsURLConnection)urlConnection, t);
				}
			}
			
			@Override
			public void success(HttpURLConnection urlConnection, Object responseData) {
				if (completion != null) {
					Error error = getError();
					if (error != null) {
						completion.failure((HttpsURLConnection)urlConnection, error);
					} else {
						completion.success((HttpsURLConnection)urlConnection, responseData);
					}
				}
			}
		});
	}
	
	@Override
	public synchronized void execute() {
		// Initiate the SSL certificate handshake
		try {
			this.urlConnection.connect();
		} catch (IOException e) {
			// TODO: return error
		}
		
		super.execute();
	}
}
