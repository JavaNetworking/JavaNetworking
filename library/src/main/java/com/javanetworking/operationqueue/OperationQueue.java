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
	private static final OperationQueue instance = new OperationQueue();
	private static final BlockingQueue<Operation> mainQueue = new LinkedBlockingQueue<Operation>();
	private Thread queueThread;
	private boolean running = true;
	
	public static void initialize() {
		getInstance().start();
	}

	public static void destroy() {
		getInstance().running = false;
		getInstance().queueThread.interrupt();
		mainQueue.clear();
	}
	
	public static OperationQueue getInstance() {
		return instance;
	}

	public static void addOperation(Operation operation) {

		if (mainQueue.offer(operation)) {
			operation.setState(OperationState.InQueue);
		} else {
			operation.setState(OperationState.Rejected);
		}

	}
	
	public static void addOperations(List<Operation> operations) {
		for (Operation operation : operations) {
			OperationQueue.addOperation(operation);
		}
	}

	private void start() {
		queueThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					
					try {
						Operation operation = null;
						try {
							operation = mainQueue.take();
							operation.setState(OperationState.Running);
							operation.execute();
							operation.setState(OperationState.Finished);
						} catch (Throwable t) {
							System.out.println("Throwable: " + t);
							operation.setState(OperationState.Cancelled);
						}
						operation.complete(operation.getState());
					} catch (Throwable t) {}
				}
			}
		});
		queueThread.start();
	}
}
