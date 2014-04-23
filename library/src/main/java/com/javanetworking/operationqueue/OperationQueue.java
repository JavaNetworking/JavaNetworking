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
