package com.javanetworking;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;

public class HTTPClientTest {

    private Charset defaultCharset = Charset.forName("UTF-8");
    private static final String BASE_URL = "http://httpbin.org/";
    private static HTTPClient client = null;
    
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
    
    private Map<String, Object> userParameters() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Fritz");
        params.put("surname", "Josef");
        params.put("age", "68");
        params.put("timeOfYear", "Summer");
        
        return params;
    }


    //
    // Tests
    //
    
    @BeforeClass
    public static void setUp() {
        client = HTTPClient.clientWithBaseURL(BASE_URL);
    }
    
    @Test
    public void testSimpleParameters() {
        
        Map<String, Object> params = userParameters();
        
        String simpleQueryParams = HTTPClient.queryStringFromParametersWithCharset(params, defaultCharset);
        
        assertNotNull(simpleQueryParams);
        assertEquals("age=68&name=Fritz&surname=Josef&timeOfYear =Summer", simpleQueryParams);
    }
    
    @Test
    public void testSimpleJSONParameters() {
        Map<String, Object> params = userParameters();
        
        String simpleQueryParams = HTTPClient.JsonStringFromMap(params);
        
        assertNotNull(simpleQueryParams);
        assertEquals("{\"age\":\"68\",\"name\":\"Fritz\",\"surname\":\"Josef\",\"timeOfYear\":\"Summer\"}", simpleQueryParams);
    }
    
    @Test
    public void testNestedParameters() {
        
        Map<String, Object> params = userParameters();
        
        HashMap<String, Object> userParams = new HashMap<String, Object>();
        userParams.put("user", params);
        
        String nestedQueryParams = HTTPClient.queryStringFromParametersWithCharset(userParams, defaultCharset);
        
        assertNotNull(nestedQueryParams);
        assertEquals("user[age]=68&user[name] =Fritz&user[surname] =Josef&user[timeOfYear] =Summer", nestedQueryParams);
    }
    
    @Test
    public void testNestedJSONParameters() {
        
        Map<String, Object> params = userParameters();
        
        HashMap<String, Object> userParams = new HashMap<String, Object>();
        userParams.put("user", params);
        
        String nestedQueryParams = HTTPClient.JsonStringFromMap(userParams);
        
        assertNotNull(nestedQueryParams);
        assertEquals("{\"user\":{\"age\":\"68\",\"name\":\"Fritz\",\"surname\":\"Josef\",\"timeOfYear\":\"Summer\"}}", nestedQueryParams);
    }
    
    @Test
    public void testMultipleNestedJSONParameters() {
        
        Map<String, Object> params = userParameters();
        
        HashMap<String, Object> userParams = new HashMap<String, Object>();
        userParams.put("user", params);
        
        List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
        users.add(userParams);
        users.add(userParams);
        users.add(userParams);
        users.add(userParams);
        
        HashMap<String, Object> usersParams = new HashMap<String, Object>();
        usersParams.put("users", users);
        
        String usersQueryParams = HTTPClient.JsonStringFromMap(usersParams);
        
        assertNotNull(usersQueryParams);
        assertEquals("{\"users\":[{\"user\":{\"age\":\"68\",\"name\":\"Fritz\",\"surname\":\"Josef\",\"timeOfYear\":\"Summer\"}},{\"user\":{\"age\":\"68\",\"name\":\"Fritz\",\"surname\":\"Josef\",\"timeOfYear\":\"Summer\"}},{\"user\":{\"age\":\"68\",\"name\":\"Fritz\",\"surname\":\"Josef\",\"timeOfYear\":\"Summer\"}},{\"user\":{\"age\":\"68\",\"name\":\"Fritz\",\"surname\":\"Josef\",\"timeOfYear\":\"Summer\"}}]}", usersQueryParams);
    }
    
    private List<Object> testArrayList() {
        ArrayList<Object> numbers = new ArrayList<Object>();
        numbers.add("1");
        numbers.add("2");
        numbers.add("3");
        numbers.add("4");
        numbers.add("5");
        numbers.add("6");
        numbers.add("7");
        numbers.add("8");
        numbers.add("9");
        numbers.add("10");
        
        return numbers;
    }
    
    @Test
    public void testArrayParameters() {
        List<Object> numbers = testArrayList();
        
        HashMap<String, Object> listParams = new HashMap<String, Object>();
        listParams.put("numbers", numbers);
        
        String listQueryParams = HTTPClient.queryStringFromParametersWithCharset(listParams, defaultCharset);
        
        assertNotNull(listQueryParams);
        assertEquals("numbers[]=1&numbers[]=2&numbers[]=3&numbers[]=4&numbers[]=5&numbers[]=6&numbers[]=7&numbers[]=8&numbers[]=9&numbers[]=10", listQueryParams);
    }
    
    @Test
    public void testArrayJSONParameters() {
        List<Object> numbers = testArrayList();
        
        HashMap<String, Object> listParams = new HashMap<String, Object>();
        listParams.put("numbers", numbers);

        String listQueryParams = HTTPClient.JsonStringFromMap(listParams);

        assertNotNull(listQueryParams);
        assertEquals("{\"numbers\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"]}", listQueryParams);
    }
    
    private HashSet<String> testSet() {
        HashSet<String> testSet = new HashSet<String>();
        testSet.add("1");
        testSet.add("2");
        testSet.add("3");
        testSet.add("4");
        testSet.add("5");
        testSet.add("6");
        testSet.add("7");
        testSet.add("8");
        testSet.add("9");
        testSet.add("10");
        
        return testSet;
    }
    
    @Test
    public void testSetParameters() {
        HashSet<String> testSet = testSet();
        
        HashMap<String, Object> setParams = new HashMap<String, Object>();
        setParams.put("numbers", testSet);
        
        String nestedSetParams = HTTPClient.queryStringFromParametersWithCharset(setParams, defaultCharset);
        
        assertNotNull(nestedSetParams);
        assertEquals("numbers=3&numbers=2&numbers=10&numbers=1&numbers=7&numbers=6&numbers=5&numbers=4&numbers=9&numbers=8", nestedSetParams);
    }
    
    @Test
    public void testSetJSONParameters() {
        HashSet<String> testSet = testSet();
        
        HashMap<String, Object> setParams = new HashMap<String, Object>();
        setParams.put("numbers", testSet);
        
        String nestedSetParams = HTTPClient.JsonStringFromMap(setParams);
        
        assertNotNull(nestedSetParams);
        assertEquals("{\"numbers\":[\"3\",\"2\",\"10\",\"1\",\"7\",\"6\",\"5\",\"4\",\"9\",\"8\"]}", nestedSetParams);
    }

    @Test
    public void testDefaultStringEncoding() {
        assertTrue(client.getStringEncoding().equals(Charset.forName("UTF-8")));
    }

    @Test
    public void testBase64Encoding() {
        assertEquals("dXNlcm5hbWU6MTIzNDU2Nzg=", HTTPClient.Base64EncodedStringFromString("username:12345678"));
    }

    @Test
    public void testBasicAuthorizationHeaderWithValidUsernameAndPassword() {
        final CountDownLatch signal = new CountDownLatch(1);

        final StringBuilder errorSB = new StringBuilder();
        final StringBuilder successSB = new StringBuilder();

        // Set basic auth header and execute GET request
        this.client.setAuthorizationHeaderWithUsernameAndPassword("username", "password");
        this.client.GET("/basic-auth/username/password", null, completionWithCountDownLatch(signal, errorSB, successSB));
        
        waitForSignalCountDown(signal);

        // Test values
        assertEquals("", errorSB.toString());
        assertTrue(errorSB.toString().isEmpty());
        assertFalse(successSB.toString().isEmpty());
    }
}
