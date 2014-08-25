	package com.javanetworking;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;

public class HTTPURLRequestOperationTest {
	
	public static final String BASE_URL = "http://httpbin.org/";
	
	@Test
	public void testHTTPURLRequestOperationSuccess() {
		final CountDownLatch signal = new CountDownLatch(1);

		final StringBuilder errorSB = new StringBuilder();
		final StringBuilder successSB = new StringBuilder();
		
		// Request and operation
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "get");
		
		HTTPURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
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
		}).start();

		// Wait for signal count down
		try {
            signal.await(30, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		// Test values
		assertEquals("", errorSB.toString());

		assertTrue(errorSB.toString().isEmpty());
		assertFalse(successSB.toString().isEmpty());
	}
	
	@Test
	public void testHTTPURLRequestOperationFailure() {
		final CountDownLatch signal = new CountDownLatch(1);

		final StringBuilder errorSB = new StringBuilder();
		final StringBuilder successSB = new StringBuilder();

		// Request and operation
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "status/404");

		HTTPURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
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
		}).start();

		// Wait for signal count down
		try {
	        signal.await(30, TimeUnit.SECONDS); // wait for callback
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }

		// Test values
		assertEquals("", successSB.toString());

		assertFalse(errorSB.toString().isEmpty());
		assertTrue(successSB.toString().isEmpty());
	}
}