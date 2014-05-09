package com.javanetworking;

import java.io.IOException;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;


public class HttpsURLConnectionOperation extends HttpURLConnectionOperation {

	public interface HttpsCompletion {
		void failure(HttpsURLConnection httpConnection, Throwable t);
		void success(HttpsURLConnection httpConnection, byte[] responseData);
	}
	
	public static HttpsURLConnectionOperation operationWithHttpsURLConnection(HttpsURLConnection urlConnection, HttpsCompletion httpsCompletion) {
		return new HttpsURLConnectionOperation(urlConnection, httpsCompletion);
	}
	
	HttpsURLConnection urlConnection;
	
	public HttpsURLConnectionOperation(HttpsURLConnection urlConnection, HttpsCompletion completion) {
		super(urlConnection, null);
		
		this.urlConnection = urlConnection;
		
		super.setHttpCompletion(completionWithHttpsCompletion(completion));
	}
	
	public HttpsURLConnection getHttpsURLConnection() {
		return this.urlConnection;
	}
	
	private HttpCompletion completionWithHttpsCompletion(final HttpsCompletion completion) {
		return new HttpCompletion() {
			@Override
			public void failure(HttpURLConnection urlConnection, Throwable t) {
				if (completion != null) {
					completion.failure((HttpsURLConnection)urlConnection, t);
				}
			}
			
			@Override
			public void success(HttpURLConnection urlConnection, byte[] responseData) {
				if (completion != null) {
					Error error = getError();
					if (error != null) {
						completion.failure((HttpsURLConnection)urlConnection, error);
					} else {
						completion.success((HttpsURLConnection)urlConnection, responseData);
					}
				}
			}
		};
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
