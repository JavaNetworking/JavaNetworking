package com.javanetworking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;

public class JSONURLRequestOperationTest {

	public static final String BASE_URL = "http://httpbin.org";
	
	@Test
	public void testOperationAcceptsApplicationJSON() {
		final CountDownLatch signal = new CountDownLatch(1);

		final StringBuilder errorSB = new StringBuilder();
		final StringBuilder successSB = new StringBuilder();
		
		// Request and operation
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=application/json");
		
		JSONURLRequestOperation operation = JSONURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				errorSB.append(t.toString());

				signal.countDown();
			}
			@Override
			public void success(URLRequest request, Object response) {
				successSB.append(response);

				signal.countDown();
			}
		});

		operation.start();

		// Wait for signal count down
		try {
            signal.await(30, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		// Test values
		assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
		assertEquals("", errorSB.toString());

		assertTrue(errorSB.toString().isEmpty());
		assertFalse(successSB.toString().isEmpty());
	}
	
	@Test
	public void testOperationAcceptsTextJSON() {
		final CountDownLatch signal = new CountDownLatch(1);

		final StringBuilder errorSB = new StringBuilder();
		final StringBuilder successSB = new StringBuilder();
		
		// Request and operation
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=text/json");
		
		JSONURLRequestOperation operation = JSONURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				errorSB.append(t.toString());

				signal.countDown();
			}
			@Override
			public void success(URLRequest request, Object response) {
				successSB.append(response);

				signal.countDown();
			}
		});

		operation.start();

		// Wait for signal count down
		try {
            signal.await(30, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		// Test values
		assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
		assertEquals("", errorSB.toString());

		assertTrue(errorSB.toString().isEmpty());
		assertFalse(successSB.toString().isEmpty());
	}
	
	@Test
	public void testOperationAcceptsTextJavaScript() {
		final CountDownLatch signal = new CountDownLatch(1);

		final StringBuilder errorSB = new StringBuilder();
		final StringBuilder successSB = new StringBuilder();
		
		// Request and operation
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=text/javascript");
		
		JSONURLRequestOperation operation = JSONURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				errorSB.append(t.toString());

				signal.countDown();
			}
			@Override
			public void success(URLRequest request, Object response) {
				successSB.append(response);

				signal.countDown();
			}
		});

		operation.start();

		// Wait for signal count down
		try {
            signal.await(30, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		// Test values
		assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
		assertEquals("", errorSB.toString());

		assertTrue(errorSB.toString().isEmpty());
		assertFalse(successSB.toString().isEmpty());
	}
	
	@Test
	public void testInvalidContentTypeError() {
		final CountDownLatch signal = new CountDownLatch(1);

		final StringBuilder errorSB = new StringBuilder();
		final StringBuilder successSB = new StringBuilder();
		
		// Request and operation
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=application/no-json");
		
		JSONURLRequestOperation operation = JSONURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				errorSB.append(t.toString());

				signal.countDown();
			}
			@Override
			public void success(URLRequest request, Object response) {
				successSB.append(response);

				signal.countDown();
			}
		});

		operation.start();

		// Wait for signal count down
		try {
            signal.await(30, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		// Test values
		assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
		assertEquals("", successSB.toString());

		assertFalse(errorSB.toString().isEmpty());
		assertTrue(successSB.toString().isEmpty());
	}
}
