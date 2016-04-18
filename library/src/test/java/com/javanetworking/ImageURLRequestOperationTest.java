package com.javanetworking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;

public class ImageURLRequestOperationTest {

    private static final String BASE_URL = "http://httpbin.org";

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
    public void testAcceptsTIFFContentType() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=image/tiff");

        ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", errorSB.toString());
        assertTrue(errorSB.toString().isEmpty());
        assertFalse(successSB.toString().isEmpty());
    }

    @Test
    public void testAcceptsJPEGContentType() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=image/jpeg");

        ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", errorSB.toString());
        assertTrue(errorSB.toString().isEmpty());
        assertFalse(successSB.toString().isEmpty());
    }

    @Test
    public void testAcceptsGIFContentType() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=image/gif");

        ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", errorSB.toString());
        assertTrue(errorSB.toString().isEmpty());
        assertFalse(successSB.toString().isEmpty());
    }

    @Test
    public void testAcceptsPNGContentType() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=image/png");

        ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", errorSB.toString());
        assertTrue(errorSB.toString().isEmpty());
        assertFalse(successSB.toString().isEmpty());
    }

    @Test
    public void testAcceptIconContentTypes() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        List<String> acceptableContentTypes = Arrays.asList("image/ico", "image/x-icon");
        for (String contentType : acceptableContentTypes) {
            URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=" + contentType);

            ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
            operation.start();

            assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.InQueue);

            waitForSignalCountDown(signal);

            // Test values
            assertEquals("", errorSB.toString());
            assertTrue(errorSB.toString().isEmpty());
            assertFalse(successSB.toString().isEmpty());
        }
    }

    @Test
    public void testAcceptBitmapContentTypes() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        List<String> acceptableContentTypes = Arrays.asList("image/bmp", "image/x-bmp", "image/x-xbitmap", "image/x-win-bitmap");
        for (String contentType : acceptableContentTypes) {
            URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=" + contentType);

            ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
            operation.start();

            assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.InQueue);

            waitForSignalCountDown(signal);

            // Test values
            assertEquals("", errorSB.toString());
            assertTrue(errorSB.toString().isEmpty());
            assertFalse(successSB.toString().isEmpty());
        }
    }

    @Test
    public void testInvalidContentType() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/response-headers?Content-Type=image/invalid");

        ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", successSB.toString());
        assertFalse(errorSB.toString().isEmpty());
        assertTrue(successSB.toString().isEmpty());
    }

    @Test
    public void testEmptySuccessResponeOnFailure() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Request and operation
        URLRequest request = URLRequest.requestWithURLString(BASE_URL + "/status/404");

        ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, completionWithCountDownLatch(signal, errorSB, successSB));
        operation.start();

        waitForSignalCountDown(signal);

        // Test values
        assertTrue(operation.getState() == HTTPURLRequestOperation.OperationState.Finished);
        assertEquals("", successSB.toString());
        assertFalse(errorSB.toString().isEmpty());
        assertTrue(successSB.toString().isEmpty());
    }
}
