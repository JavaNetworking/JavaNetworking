package com.javanetworking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 Wrapper class for a {@link URLConnection} request. Holds the string URL, URLConnection
 request and HTTP body content for current request.
 */
public class URLRequest {

	private String urlString;
	private URLConnection urlConnection;
	private byte[] HTTPBody;
	private Exception error;
	private InputStream inputStream;
	
	public static URLRequest requestWithURLString(String url) {
		return new URLRequest(url);
	}
	
	/**
	 Contructor for a new request.
	 
	 @param urlString A {@link String} representation of the resource URL.
	 */
	public URLRequest(String urlString) {
		this.urlString = urlString;
		this.urlConnection = null;
		this.HTTPBody = null;
	}
	
	public byte[] getHTTPBody() {
		return HTTPBody;
	}

	public void setHTTPBody(byte[] HTTPBody) {
		this.HTTPBody = HTTPBody;
	}

	public Exception getException() {
		return this.error;
	}

	public URLConnection getURLConnection() {
		if (urlConnection == null) {
			try {
				urlConnection = new URL(this.urlString).openConnection();
			} catch (Exception e) {
				this.error = e;
			}
		}
		return urlConnection;
	}
	
	public HttpURLConnection getHttpURLConnection() {
		return ((HttpURLConnection)getURLConnection());
	}

	public void setRequestProperty(String key, String value) {
		getURLConnection().setRequestProperty(key, value);
	}

	public void setDoOutput(boolean b) {
		getURLConnection().setDoOutput(b);
	}

	public OutputStream getOutputStream() throws IOException {
		return getURLConnection().getOutputStream();
	}

	public InputStream getInputStream() throws IOException {
		return getURLConnection().getInputStream();
	}

	public int getResponseCode() throws IOException {
		return getHttpURLConnection().getResponseCode();
	}

	public String getContentType() {
		return getHttpURLConnection().getContentType();
	}

	public void setRequestMethod(String method) {
		try {
			getHttpURLConnection().setRequestMethod(method);
		} catch (ProtocolException e) {
			this.error = e;
		}
	}

	public void setConnectTimeout(int timeout) {
		getHttpURLConnection().setReadTimeout(timeout);
	}

	public Map<String, List<String>> getHeaderFields() {
		return getHttpURLConnection().getHeaderFields();
	}

	public void setHTTPBodyStream(InputStream bodyStream) {
		this.inputStream = bodyStream;
	}
}
