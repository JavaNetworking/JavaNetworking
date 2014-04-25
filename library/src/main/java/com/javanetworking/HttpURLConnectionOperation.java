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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import com.javanetworking.operationqueue.BaseOperation;
import com.javanetworking.operationqueue.OperationQueue;

public class HttpURLConnectionOperation extends BaseOperation {
	
	public interface Completion {
		void failure(HttpURLConnection urlConnection, Throwable t);
		void success(HttpURLConnection urlConnection, byte[] responseData);
	}
	
	public static HttpURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, Completion completion) {
		return new HttpURLConnectionOperation(urlConnection, completion);
	}
	
	private HttpURLConnection urlConnection;
	private Completion completion;
	private InputStream inputStream;
	private ByteArrayOutputStream accumulationBuffer;
	
	private HttpURLConnectionOperation(HttpURLConnection urlConnection, Completion completion) {
		super();
		
		this.urlConnection = urlConnection;
		this.completion = completion;

		this.accumulationBuffer = new ByteArrayOutputStream();
	}
	
	public void start() {
		OperationQueue queue = new OperationQueue();
		queue.addOperation(this);
	}
	
	@Override
	public synchronized void execute() {
		super.execute();
		
		try {
			this.inputStream = urlConnection.getInputStream();
			BufferedInputStream bin = new BufferedInputStream(this.inputStream);
			
			int c;
			while (-1 != (c = bin.read())) {
				this.accumulationBuffer.write(c);
			}
			bin.close();
			this.inputStream.close();
			this.inputStream = null;
		} catch (IOException e) {
			completion.failure(urlConnection, e);
		}
	}
	
	@Override
	public synchronized void complete() {
		super.complete();
		
		switch (getState()) {
			case Rejected:
				completion.failure(urlConnection, new Throwable("Rejected"));
				break;
			case Cancelled:
				completion.failure(urlConnection, new Throwable("Cancelled"));
				break;
			default:
				completion.success(urlConnection, this.accumulationBuffer.toByteArray());
				break;
		}
		
		try {
			this.accumulationBuffer.close();
			this.accumulationBuffer = null;
		} catch (IOException e) {}

	}
}
