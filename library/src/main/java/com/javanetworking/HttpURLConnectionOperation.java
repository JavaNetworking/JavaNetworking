package com.javanetworking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import com.javanetworking.operationqueue.BaseOperation;
import com.javanetworking.operationqueue.OperationQueue;

public class HttpURLConnectionOperation extends BaseOperation {
	
	public interface Completion {
		void failure(HttpURLConnection urlConnection, Throwable t);
		void success(HttpURLConnection urlConnection, Object responseData);
	}
	
	public static HttpURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, Completion completion) {
		return new HttpURLConnectionOperation(urlConnection, completion);
	}
	
	private HttpURLConnection urlConnection;
	private Completion completion;
	private StringBuffer accumulationBuffer;
	
	private HttpURLConnectionOperation(HttpURLConnection urlConnection, Completion completion) {
		super();
		
		this.urlConnection = urlConnection;
		this.completion = completion;
	}
	
	public void start() {
		OperationQueue.initialize();
		OperationQueue.addOperation(this);
	}
	
	@Override
	public void execute() throws Throwable {
		super.execute();
		
		System.out.println("\nSending '" + urlConnection.getRequestMethod() + "' request to URL : " + urlConnection);
		System.out.println("Response Code : " + urlConnection.getResponseCode());
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(urlConnection.getInputStream()));
		String inputLine;
		
		accumulationBuffer = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			accumulationBuffer.append(inputLine);
		}
		in.close();
	}
	
	@Override
	public void complete(OperationState state) {
		super.complete(state);
		
		switch (state) {
			case Rejected:
				completion.failure(urlConnection, new Throwable("Rejected"));
				break;
			case Cancelled:
				completion.failure(urlConnection, new Throwable("Cancelled"));
				break;
			default:
				completion.success(urlConnection, new String(accumulationBuffer));
				break;
		}
		
		OperationQueue.destroy();
	}
}
