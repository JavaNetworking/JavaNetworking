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

/**
 The {@link OperationsQueue} class handles the execution of {@link Operation} instances
 through a queue and executes them one by one in a separate thread.
 */
public class OperationQueue {

	/**
	 A {@link BlockingQueue} instance which acts at the main queue that holds added operations.
	 */
	private final BlockingQueue<Operation> mainQueue;

	/**
	 A {@link Thread} instance that takes and executes operations from the mainQueue.
	 */
	private Thread queueThread;

	/**
	 A boolean value inticating the running status of the queueThread. When set to false the queueThread
	 exits when current operation finnishes.
	 */
	private boolean runningStatus = false;
	
  
	//----------------------------------------
	// @name Constructor and Instance creation
	//----------------------------------------

	/**
	 Instantiate a new {@link OperationQueue} instance
	 */
	public OperationQueue() {
		this.mainQueue = new LinkedBlockingQueue<Operation>();
	}
	
	
	//-----------------------------------
	// @name Attribute and status methods
	//-----------------------------------

	/**
	 Set the running status of current execution thread.
	 
	 @param status A boolean value indication if the running status should be true or false
	 			   if false the current execution thread will exit after the next operations
	 			   finishes. 
	 */
	public synchronized void setRunningStatus(boolean status) {
		this.runningStatus = status;
	}
	
	/**
	 Return the running status boolean value.
	 
	 @return A boolean value indicating the running status of the current execution thread.
	 		 If false the execution thread has been stopped, and will be started if another operation
	 		 is added to the current instance of the {@link OperationQueue}.
	 */
	public synchronized boolean getRunningStatus() {
		return this.runningStatus;
	}
	
	/**
	 Cancel all waiting operations and clear operations queue. This method sets the running
	 status to false, interrupts the current execution thread which throws an {@link InterruptedException}
	 and exits before finally clearing the main queue for waiting operations.
	 */
	public void cancelAllOperations() {
		setRunningStatus(false);
		this.queueThread.interrupt();
		this.mainQueue.clear();
	}
	
	/**
	 Add {@link Operation} instance to the {@link OperationQueue} instance main queue. The {@linkplain}
	 operations if offered to the main queue but can be rejected and not added to the queue. When 
	 rejected the {@link Operation} has the state of {@link OperationState.Rejected}. If the {@link Operation}
	 is added to the queue the state is set to {@link OperationState.InQueue}.
	 
	 It the {@link OperationQueue}s running status is false. The execution thread is started and the running
	 status is updated to true.
	 */
	public void addOperation(Operation operation) {
		if (getRunningStatus() != true) {
			start();
		}
		
		if (mainQueue.offer(operation)) {
			operation.setState(OperationState.InQueue);
		} else {
			operation.setState(OperationState.Rejected);
		}
	}
	
	/**
	 Adds a {@link List} of instantiated {@link Operation} objects. The methods loops over the list
	 and calls the {@code addOperation(Operation operation)} method.
	 
	 @param operations An instantiated {@link List} of instantiated {@link Operation} objects which is
	 		added to the {@link OperationQueue}s main queue.
	 */
	public void addOperations(List<Operation> operations) {
		for (Operation operation : operations) {
			this.addOperation(operation);
		}
	}
	
	/**
	 Check if the {@link OperationQueue}s main queue is empty.
	 
	 @return A boolean value indicating if the {@link OperationQueue} is empty. If true the {@link OperationQueue}
	 		 has no waiting operations to execute.
	 */
	public boolean isEmpty() {
		return mainQueue.isEmpty();
	}
	

	//----------------------------------------------
	// @name Execution thread and operation handling
	//----------------------------------------------

	/**
	 Private method to instantiate and start the queueThread which takes {@link Operation} objects
	 from the main queue when they are ready. The {@link BlockingQueue}s {@code take()} method is
	 used since it retrieves and removes the head of main queue, waiting if necessary until an element 
	 becomes available.
	 */
	private void start() {
		queueThread = new Thread(new Runnable() {
			@Override
			public void run() {
				setRunningStatus(true);
				
				while (getRunningStatus()) {
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
						setRunningStatus(false);
					}
				}
				
			}
		});
		queueThread.start();
	}
}
