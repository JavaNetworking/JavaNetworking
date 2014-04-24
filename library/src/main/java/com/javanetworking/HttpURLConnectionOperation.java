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
	private ByteArrayOutputStream accumulationBuffer;
	
	private HttpURLConnectionOperation(HttpURLConnection urlConnection, Completion completion) {
		super();
		
		this.urlConnection = urlConnection;
		this.completion = completion;

		this.accumulationBuffer = new ByteArrayOutputStream();
	}
	
	public void start() {
		OperationQueue.initialize();
		OperationQueue.addOperation(this);
	}
	
	@Override
	public void execute() throws Throwable {
		super.execute();
		
		BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
		
		int c;
		while ((c = in.read()) != -1) {
			this.accumulationBuffer.write(c);
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
				completion.success(urlConnection, this.accumulationBuffer.toByteArray());
				break;
		}
		try {
			this.accumulationBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		OperationQueue.destroy();
	}
}
