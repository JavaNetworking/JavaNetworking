package com.javanetworking.operationqueue;

public class BaseOperation implements Operation {

	private OperationState state = null;
	
	public BaseOperation() {
		setState(OperationState.Created);
	}
	
	protected String getName() {
		return BaseOperation.class.getName();
	}
	
	public void setState(OperationState state) {
		this.state = state;
	}
	
	public OperationState getState() {
		return state;
	}

	public void execute() throws Throwable {}
	public void complete(OperationState state) {}
}
