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

package com.javanetworking.operationqueue;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.javanetworking.operationqueue.Operation.OperationState;


public class OperationQueue {

	private final BlockingQueue<Operation> mainQueue;
	private Thread queueThread;
	private boolean running = false;
	private final Object lock = new Object();
	
	public OperationQueue() {
		this.mainQueue = new LinkedBlockingQueue<Operation>();
	}
	
	public void cancelAllOperations() {
		synchronized (lock) {
			this.running = false;
		}
		this.queueThread.interrupt();
		this.mainQueue.clear();
	}
	
	public void addOperation(Operation operation) {
		synchronized (lock) {
			if (!this.running) {
				start();
			}
		}
		
		if (mainQueue.offer(operation)) {
			operation.setState(OperationState.InQueue);
		} else {
			operation.setState(OperationState.Rejected);
		}
	}
	
	public boolean isEmpty() {
		return mainQueue.isEmpty();
	}
	
	public void addOperations(List<Operation> operations) {
		for (Operation operation : operations) {
			this.addOperation(operation);
		}
	}
	
	private void start() {
		queueThread = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					OperationQueue.this.running = true;
					
					while (running) {
						try {
							Operation operation = null;
							try {
								operation = mainQueue.take();
								operation.setState(OperationState.Running);
								operation.execute();
								operation.setState(OperationState.Finished);
							} catch (Throwable t) {
								operation.setState(OperationState.Cancelled);
							}
							operation.complete();
						} catch (Throwable t) {}
						
						if (OperationQueue.this.mainQueue.isEmpty()) {
							OperationQueue.this.running = false;
						}
					}
				}
			}
		});
		queueThread.start();
	}
}
