    package com.javanetworking;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;

public class HTTPURLRequestOperationTest {
    
    public static final String BASE_URL = "http://httpbin.org";
    
    private HTTPCompletion completionWithCountDownLatch(final CountDownLatch signal, final StringBuilder errorSB, final StringBuilder successSB) {
        return new HTTPCompletion() {
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
        };
    }

    private void waitForSignalCountDown(CountDownLatch signal) {
        try {
            signal.await(30, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHTTPURLRequestOperationSuccess() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/get");

        HTTPURLRequestOperation operation = HTTPURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
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
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/status/404");

        HTTPURLRequestOperation operation = HTTPURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", successSB.toString());

        assertFalse(errorSB.toString().isEmpty());
        assertTrue(successSB.toString().isEmpty());
    }

    @Test
    public void test500StatusCodeError() throws MalformedURLException {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/status/500");

        HTTPURLRequestOperation operation = HTTPURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", successSB.toString());

        assertFalse(errorSB.toString().isEmpty());
        assertTrue(successSB.toString().isEmpty());
    }
}