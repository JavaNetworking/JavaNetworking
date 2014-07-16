package com.javanetworking;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class URLRequest extends HttpURLConnection {

	private byte[] HTTPBody;
	
	public static URLRequest requestWithURLString(String url) {
		HttpURLConnection request = null;
		try {
			request = (HttpURLConnection) new URL(url).openConnection();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return (URLRequest) request;
	}
	
	protected URLRequest(URL url) {
		super(url);	
	}
	
	public byte[] getHTTPBody() {
		return HTTPBody;
	}

	public void setHTTPBody(byte[] hTTPBody) {
		HTTPBody = hTTPBody;
	}
	
	@Override
	public void setRequestMethod(String method) {
		try {
			super.setRequestMethod(method);
		} catch (ProtocolException e) {}
	}
	
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
