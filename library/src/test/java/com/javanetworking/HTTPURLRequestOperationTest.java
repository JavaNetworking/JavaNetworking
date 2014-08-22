	package com.javanetworking;

import static org.junit.Assert.*;

import org.junit.*;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;

public class HTTPURLRequestOperationTest {
	
	public static final String BASE_URL = "http://httpbin.org/";
	
	@Test
	public void testHTTPURLRequestOperationSuccess() {
		
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "get");
		
		HTTPURLRequestOperation operation = HTTPURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				assertNull(t);
			}
			@Override
			public void success(URLRequest request, Object response) {
				assertNotNull(response);
			}
		});
		
		operation.start();
	}
	
	@Test
	public void testHTTPURLRequestOperationFailure() {
		
		URLRequest request = URLRequest.requestWithURLString(BASE_URL + "status/404");
		
		HTTPURLRequestOperation operation = HTTPURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
			@Override
			public void failure(URLRequest request, Throwable t) {
				assertNotNull(t);
			}
			@Override
			public void success(URLRequest request, Object response) {
				assertNull(response);
			}
		});
		
		operation.start();
	}
}