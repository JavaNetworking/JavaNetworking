package com.javanetworking.operationqueue;

public interface Operation {
	enum OperationState {
		Created,
		Rejected,
		InQueue,
		Running,
		Cancelled,
		Finished
	}

	void setState(OperationState state);
	OperationState getState();
	
	void execute() throws Throwable;
	void complete(OperationState state);
}
