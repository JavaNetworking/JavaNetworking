package com.javanetworking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;


public class URLRequest {

	private String url;
	private URLConnection urlConnection;
	private byte[] HTTPBody;

	
	public static URLRequest requestWithURLString(String url) {
		return new URLRequest(url);
	}
	
	
	public URLRequest(String url) {
		this.url = url;
		this.urlConnection = null;
		this.HTTPBody = null;
	}
	
	public byte[] getHTTPBody() {
		return HTTPBody;
	}

	public void setHTTPBody(byte[] hTTPBody) {
		HTTPBody = hTTPBody;
	}

	public URLConnection getURLConnection() {
		if (urlConnection == null) {
			try {
				urlConnection = new URL(this.url).openConnection();
			} catch (MalformedURLException e) {
				
			} catch (IOException e) {
				
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
		} catch (ProtocolException e) {}
	}

	public void setConnectTimeout(int timeout) {
		getHttpURLConnection().setReadTimeout(timeout);
	}

	public Map<String, List<String>> getHeaderFields() {
		return getHttpURLConnection().getHeaderFields();
	}
}
