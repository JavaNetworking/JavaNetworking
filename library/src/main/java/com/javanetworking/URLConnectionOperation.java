package com.javanetworking;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.operationqueue.BaseOperation;
import com.operationqueue.Operation;
import com.operationqueue.OperationQueue;

/**
 {@link URLConnectionOperation} is an extension of {@link BaseOperation} that implements the {@link Operation} interface.
 */
public class URLConnectionOperation extends BaseOperation {

	/**
	 {@link URLCompletion} is {@link URLConnectionOperation}s completion interface which indicates the
	 {@link URLConnection} failed or succeeded.
	 */
	public interface URLCompletion {
		void failure(URLConnection urlConnection, Throwable t);
		void success(URLConnection urlConnection, byte[] responseData);
	}

	/**
	 A static constructor method that creates and returns a {@link URLConnectionOperation} instance.
	 */
	public static URLConnectionOperation operationWithHttpURLConnection(URLConnection urlConnection, String requestBody, URLCompletion completion) {
		return new URLConnectionOperation(urlConnection, requestBody, completion);
	}

	/**
	 The URL connection which holds the request.
	 */
	private URLConnection urlConnection;

	/**
	 The instance completion interface.
	 */
	private URLCompletion completion;

	/**
	 Response accumulation buffer, a byte array output stream.
	 */
	private ByteArrayOutputStream accumulationBuffer;

	/**
	 HTTP request body string. Written to server UTF-8 encoded.
	 */
    private String requestBody;

	/**
	 Instantiates this class and sets the {@link URLConnection} to use, and the {@link URLCompletion} interface.

	 This is the preferred constructor.

	 @param urlConnection An open {@link URLConnection} to be used for network access.
     @param requestBody A string representation of POST/PUT HTTP request body.
	 @param completion A {@link URLCompletion} instance that handles the completion interface methods.
	 */
	public URLConnectionOperation(URLConnection urlConnection, String requestBody, URLConnectionOperation.URLCompletion completion) {
		super();

        setRequestBody(requestBody);

		setURLConnection(urlConnection);

		setURLCompletion(completion);

		this.accumulationBuffer = new ByteArrayOutputStream();
	}

	/**
	 Returns the current {@link URLConnection} used.

	 @return The {@link URLConnection} of this operation.
	 */
	public URLConnection getURLConnection() {
		return this.urlConnection;
	}

	/**
	 Sets the current {@link URLConnection}. Cannot be null.
	 */
	public void setURLConnection(URLConnection urlConnection) {
		if (urlConnection == null) {
			throw new NullPointerException();
		}
		this.urlConnection = urlConnection;
	}

	/**
	 Sets the {@link URLCompletion} interface that responds to this operation.
	 */
	protected void setURLCompletion(URLCompletion completion) {
		this.completion = completion;
	}

    protected void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
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
            if (this.requestBody != null) {
                this.urlConnection.setRequestProperty("charset", "utf-8");
                this.urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(requestBody.getBytes().length));
                this.urlConnection.setDoOutput(true);

                this.urlConnection.getOutputStream().write(requestBody.getBytes(Charset.forName("UTF-8")));
            }
            this.urlConnection.connect();

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

	/**
	 The complete method is called when this operation finishes executing.
	 */
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
