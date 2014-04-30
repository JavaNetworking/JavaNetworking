package com.javanetworking;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import com.javanetworking.operationqueue.BaseOperation;
import com.javanetworking.operationqueue.OperationQueue;

public class URLConnectionOperation extends BaseOperation {
	
	public interface URLCompletion {
		void failure(URLConnection urlConnection, Throwable t);
		void success(URLConnection urlConnection, byte[] responseData);
	}
	
	public static URLConnectionOperation operationWithHttpURLConnection(URLConnection urlConnection, URLCompletion completion) {
		return new URLConnectionOperation(urlConnection, completion);
	}
	
	private URLConnection urlConnection;
	private URLCompletion completion;
	private ByteArrayOutputStream accumulationBuffer;
	
	
	public URLConnectionOperation(URLConnection urlConnection, URLConnectionOperation.URLCompletion completion) {
		super();
		
		this.urlConnection = urlConnection;
		
		setURLCompletion(completion);

		this.accumulationBuffer = new ByteArrayOutputStream();
	}
	
	public URLConnection getURLConnection() {
		return this.urlConnection;
	}
	
	public void setURLCompletion(URLCompletion completion) {
		this.completion = completion;
	}
	
	
	public void start() {
		OperationQueue queue = new OperationQueue();
		queue.addOperation(this);
	}
	
	@Override
	public synchronized void execute() {
		super.execute();
		
		try {
			InputStream is = urlConnection.getInputStream();
			BufferedInputStream bin = new BufferedInputStream(is);
			
			int c;
			while (-1 != (c = bin.read())) {
				this.accumulationBuffer.write(c);
			}
			bin.close();
			is.close();
			
		} catch (IOException e) {
			if (this.completion != null) {
				this.completion.failure(this.urlConnection, e);
			}
		}
	}
	
	@Override
	public synchronized void complete() {
		super.complete();
		
		switch (getState()) {
			case Rejected:
				if (this.completion != null) {
					this.completion.failure(this.urlConnection, new Throwable("URLConnectionOperation rejected from operation queue"));
				}
				break;
			case Cancelled:
				if (this.completion != null) {
					this.completion.failure(this.urlConnection, new Throwable("URLConnectionOperation cancelled in operation queue"));
				}
				break;
			default:
				if (this.completion != null) {
					this.completion.success(this.urlConnection, this.accumulationBuffer.toByteArray());
				}
				break;
		}
		
		try {
			this.accumulationBuffer.close();
			this.accumulationBuffer = null;
		} catch (IOException e) {}

	}
}
