package com.javanetworking;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.operationqueue.BaseOperation;
import com.operationqueue.Operation;
import com.operationqueue.OperationQueue;

/**
 {@link URLRequestRequest} is an extension of {@link BaseOperation} that implements the {@link Operation} interface.
 */
public class URLConnectionOperation extends BaseOperation {

	/**
	 {@link URLCompletion} is {@link URLRequestRequest}s completion interface which indicates the
	 {@link URLRequest} failed or succeeded.
	 */
	public interface URLCompletion {
		void failure(URLRequest urlRequest, Throwable t);
		void success(URLRequest urlRequest, byte[] responseData);
	}

	/**
	 A static constructor method that creates and returns a {@link URLRequestRequest} instance.
	 */
	public static URLConnectionOperation operationWithURLRequest(URLRequest urlRequest, URLCompletion completion) {
		return new URLConnectionOperation(urlRequest, completion);
	}

	/**
	 The URL request.
	 */
	private URLRequest urlRequest;

	/**
	 The instance completion interface.
	 */
	private URLCompletion completion;

	/**
	 Response accumulation buffer, a byte array output stream.
	 */
	private ByteArrayOutputStream accumulationBuffer;

	/**
	 Instantiates this class and sets the {@link URLRequest} to use, and the {@link URLCompletion} interface.

	 This is the preferred constructor.

	 @param urlRequest An open {@link URLRequest} to be used for network access.
     @param completion A {@link URLCompletion} instance that handles the completion interface methods.
	 */
	public URLConnectionOperation(URLRequest urlRequest, URLCompletion completion) {
		super();

		setURLRequest(urlRequest);

		setURLCompletion(completion);

		this.accumulationBuffer = new ByteArrayOutputStream();
	}

	/**
	 Returns the current {@link URLRequest} used.

	 @return The {@link URLRequest} of this operation.
	 */
	public URLRequest getURLRequest() {
		return this.urlRequest;
	}

	/**
	 Sets the current {@link URLRequest}. Cannot be null.
	 */
	public void setURLRequest(URLRequest urlRequest) {
		if (urlRequest == null) {
			throw new NullPointerException();
		}
		this.urlRequest = urlRequest;
	}

	/**
	 Sets the {@link URLCompletion} interface that responds to this operation.
	 */
	protected void setURLCompletion(URLCompletion completion) {
		this.completion = completion;
	}

	/**
	 Creates a new {@link OperationQueue} and adds this class which executes this operation.
	 */
	public void start() {
		OperationQueue queue = new OperationQueue();
		queue.addOperation(this);
	}

	/**
	 The executing method of this operation.

	 This method runs in the worker thread of this operations operation queue.
	 */
	@Override
	public synchronized void execute() {
		super.execute();

		try {
            // Write requestBody if any
            if (this.urlRequest.getHTTPBody() != null) {
                this.urlRequest.setRequestProperty("Content-Length", "" + Integer.toString(this.urlRequest.getHTTPBody().length));
                this.urlRequest.setDoOutput(true);

                this.urlRequest.getOutputStream().write(this.urlRequest.getHTTPBody());
            }
            
			InputStream is = urlRequest.getInputStream();
			BufferedInputStream bin = new BufferedInputStream(is);

			int c;
			while (-1 != (c = bin.read())) {
				this.accumulationBuffer.write(c);
			}
			bin.close();
			is.close();

		} catch (IOException e) {
			if (this.completion != null) {
				this.completion.failure(this.urlRequest, e);
			}
		}
	}

	/**
	 The complete method is called when this operation finishes executing.
	 */
	@Override
	public synchronized void complete() {
		super.complete();

		switch (getState()) {
			case Rejected:
				if (this.completion != null) {
					this.completion.failure(this.urlRequest, new Throwable("URLRequestOperation rejected from operation queue"));
				}
				break;
			case Cancelled:
				if (this.completion != null) {
					this.completion.failure(this.urlRequest, new Throwable("URLRequestOperation cancelled in operation queue"));
				}
				break;
			default:
				if (this.completion != null) {
					this.completion.success(this.urlRequest, this.accumulationBuffer.toByteArray());
				}
				break;
		}

		try {
			this.accumulationBuffer.close();
			this.accumulationBuffer = null;
		} catch (IOException e) {}

	}

	/**
	 The failure method is called when the operation failed in working thread.
	 */
	@Override
	public synchronized void failure(Throwable t) {
		super.failure(t);

		if (this.completion != null) {
			this.completion.failure(this.urlRequest, t);
		}
	}
}
