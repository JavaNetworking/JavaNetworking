// Copyright (c) 2014 JavaNetworking (https://github.com/JavaNetworking/JavaNetworking)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.javanetworking;

import com.javanetworking.HTTPURLRequestOperation.HTTPCompletion;
import com.javanetworking.gson.Gson;
import com.operationqueue.OperationQueue;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 {@link HTTPClient} encapsulates the modern HTTP patterns used for data exchange.
 */
public class HTTPClient {

	/**
	 The URL used for constructing the URL request.
	 */
    private String baseURL;

    /**
     The String encoding used in URL requests. Defaults to {@code Charset.forName("UTF-8")}
     */
    private Charset stringEncoding;

    /**
     {@link HTTPClientParameterEncoding} value indicate how HTTP body parameters are
		encoded for requests other than 'GET', 'HEAD' and 'DELETE'.

     Defaults to {@code HTTPClientParameterEncoding.FormURLParameterEncoding}
     */
    public enum HTTPClientParameterEncoding {
        FormURLParameterEncoding,
        JSONParameterEncoding
    }
    private HTTPClientParameterEncoding parameterEncoding;

    /**
     The {@link OperationQueue} used to handle enqueue {@link URLConnectionOperation}s for client.
     */
    private OperationQueue operationQueue;

    /**
     {@link Map<String, String>} of default headers used when constructing HTTP requests.
     */
    private Map<String, String> defaultHeaders;

    /**
	 Attempts to register a subclasses of {@link HTTPURLRequestOperation}.
	 
	 Falls back to {@code HTTPURLRequestOperation.class}, if registering of operation class fails. 
	 
	 Defaults to {@code HTTPURLRequestOperation.class}.
     */
    private List<String> registeredOperationClassNames;

    /**
     Static contructor.
     */
    public static HTTPClient clientWithBaseURL(String baseURL) {
        return new HTTPClient(baseURL);
    }

    /**
     Default constructor.
     */
    public HTTPClient(String baseURL) {
        if (baseURL == null) {
            throw new NullPointerException("baseURL cannot be null.");
        }
        if (baseURL.isEmpty()) {
            throw new IllegalArgumentException("baseURL connot be empty");
        }
        if (!(baseURL.charAt(baseURL.length()-1) == '/')) {
			baseURL = String.format("%s/", baseURL);
		}
        this.baseURL = baseURL;

        stringEncoding = Charset.forName("UTF-8");
        parameterEncoding = HTTPClientParameterEncoding.FormURLParameterEncoding;

        this.registeredOperationClassNames = new ArrayList<String>();
        this.registeredOperationClassNames.add(HTTPURLRequestOperation.class.getSimpleName());

        this.defaultHeaders = new HashMap<String, String>();

        String javaCommand = System.getProperty("sun.java.command");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String javaVersion = System.getProperty("java.version");        
        this.setDefaultHeader("User-Agent", String.format("%s (%s %s) Java/%s", javaCommand, osName, osVersion, javaVersion));

        this.operationQueue = new OperationQueue();
    }

    @Override
    public String toString() {
        return String.format("<HTTPClient baseURL:%s>", this.baseURL);
    }

    /**
     Gets the current clients {@link OperationQueue}.
     */
    public OperationQueue getOperationQueue() {
		return operationQueue;
	}

    public boolean registerHTTPOperationClass(Class<?> operationClass) {
        if (operationClass.isAssignableFrom(HTTPURLRequestOperation.class)) {
            return false;
        }

        if (this.registeredOperationClassNames.size() == 0) {
			this.registeredOperationClassNames.add(operationClass.getSimpleName());
		} else {
			this.registeredOperationClassNames.set(0, operationClass.getSimpleName());
		}

        return true;
    }

    public void setDefaultHeader(String header, String value) {
        this.defaultHeaders.put(header, value);
    }

    public void setParameterEncoding(HTTPClientParameterEncoding parameterEncoding) {
        this.parameterEncoding = parameterEncoding;
    }

    public static String queryStringFromParametersWithCharset(Map<String, Object> parameters, Charset stringEncoding) {
        StringBuilder stringBuilder = new StringBuilder();
        
        List<QueryStringPair> paramPairs = QueryStringPairsFromMap(parameters);
        for (int i=0; i<paramPairs.size(); i++) {
        	QueryStringPair pair = paramPairs.get(i);
        	
            stringBuilder.append(pair.URLEncodedStringValueWithEncoding(stringEncoding));
            if (i!=paramPairs.size()-1) {
            	stringBuilder.append('&');
			}
        }

        return stringBuilder.toString();
    }

    public static List<QueryStringPair> QueryStringPairsFromMap(Map<String, Object> parameters) {
        return QueryStringPairsFromKeyAndValue(null, parameters);
    }

    public static List<QueryStringPair> QueryStringPairsFromKeyAndValue(String key, Object value) {
        List<QueryStringPair> queryStringComponents = new ArrayList<QueryStringPair>();

        if (value instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) value;

            Set<String> nestedKeys = map.keySet();
            for (String nestedKey : nestedKeys) {
                Object nestedValue = map.get(nestedKey);
                if (nestedValue != null) {
                    queryStringComponents.addAll(QueryStringPairsFromKeyAndValue(((key != null) ? String.format("%s[%s]", key, nestedKey) : nestedKey), nestedValue));
                }
            }
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object nestedValue : list) {
                queryStringComponents.addAll(QueryStringPairsFromKeyAndValue(String.format("%s[]", key), nestedValue));
            }
        } else if (value instanceof Set) {
            Set<?> set = (Set<?>) value;
            for (Object object : set) {
                queryStringComponents.addAll(QueryStringPairsFromKeyAndValue(key, object));
            }
        } else {
            queryStringComponents.add(new QueryStringPair(key, value));
        }

        return queryStringComponents;
    }

	public static String JsonStringFromMap(Map<String, Object> parameters) {
		return new Gson().toJson(parameters);
    }

    public static String encode(String input, Charset stringEncoding) {
        if (input == null) {
			return "";
		}
    	StringBuilder resultStr = new StringBuilder();
        
        input = new String(Charset.forName(stringEncoding.name()).encode(input).array());

        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:!;=?@<>#%".indexOf(ch) >= 0;
    }

    private static String PercentEscapedQueryStringKeyFromStringWithEncoding(String field, Charset stringEncoding) {
        return HTTPClient.encode(field, stringEncoding);
    }

    private static String PercentEscapedQueryStringValueFromStringWithEncoding(String field, Charset stringEncoding) {
        return HTTPClient.encode(field, stringEncoding);
    }

    private static class QueryStringPair {

        private String field;
        private String value;

        public QueryStringPair(String field, Object value) {
            this.field = field;
            this.value = (String)value;
        }

        public String URLEncodedStringValueWithEncoding(Charset stringEncoding) {
            if (this.value == null) {
                return PercentEscapedQueryStringKeyFromStringWithEncoding(field, stringEncoding);
            } else {
                return String.format("%s=%s", PercentEscapedQueryStringKeyFromStringWithEncoding(field, stringEncoding),
                        PercentEscapedQueryStringValueFromStringWithEncoding(value, stringEncoding));
            }
        }
    }

    public void enqueueHTTPURLConnectionOperation(HTTPURLRequestOperation operation) {
        this.operationQueue.addOperation(operation);
    }

    public URLRequest connectionWithMethodPathAndParameters(String method, String path, Map<String, Object> parameters) {
    	if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
        String urlString = String.format("%s%s", this.baseURL, path);
        
        // Add GET/HEAD/DELETE parameters to URL string
        if (parameters != null && (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("DELETE"))) {
        	urlString = String.format("%s%c%s", urlString, (urlString.contains("?") ? '&' : '?'), HTTPClient.queryStringFromParametersWithCharset(parameters, this.stringEncoding));
        }
        
        URLRequest request = URLRequest.requestWithURLString(urlString);
        request.setRequestMethod(method);
        request.setConnectTimeout(500);
        for (String key : this.defaultHeaders.keySet()) {
        	request.setRequestProperty(key, this.defaultHeaders.get(key));
        }
        
        // Set POST/PUT requestBody on operation 
        if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
        	String charset = this.stringEncoding.name();

            switch (this.parameterEncoding) {
                case FormURLParameterEncoding:
                	request.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded; charset=%s", charset));
                    request.setHTTPBody(HTTPClient.queryStringFromParametersWithCharset(parameters, this.stringEncoding).getBytes(this.stringEncoding));
                    break;
                case JSONParameterEncoding:
                	request.setRequestProperty("Content-Type", String.format("application/json; charset=%s", charset));
                    request.setHTTPBody(HTTPClient.JsonStringFromMap(parameters).getBytes(this.stringEncoding));
                    break;
            }
        }

        return request;
    }

    public HTTPURLRequestOperation operationWithURLRequest(URLRequest request, HTTPCompletion completion) {

        HTTPURLRequestOperation operation = null;

        for (String className : this.registeredOperationClassNames) {
            try {
                Class<?> cl = Class.forName("com.javanetworking."+className);
                Constructor<?>[] constructors = cl.getConstructors();
                
                Class<?>[] paramsTypes = null;
                for (Constructor<?> constructor : constructors) {
                	paramsTypes = constructor.getParameterTypes();
                	if (paramsTypes.length == 2) {
                		break;
					}
				}
                Constructor<?> constructor = cl.getConstructor(paramsTypes[0], paramsTypes[1]);
                operation = (HTTPURLRequestOperation) constructor.newInstance(request, null);
                operation.setCompletion(completion);
                
                break;
                
            } catch (Exception e) {
				operation = HTTPURLRequestOperation.operationWithURLRequest(request, completion);
				break;
			}
        }
        
        return operation;
    }

    public void GET(String path, Map<String, Object> parameters, HTTPCompletion completion) {
        URLRequest request = this.connectionWithMethodPathAndParameters("GET", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.enqueueHTTPURLConnectionOperation(operation);
    }

    public void POST(String path, Map<String, Object> parameters, HTTPCompletion completion) {
    	URLRequest request = this.connectionWithMethodPathAndParameters("POST", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.enqueueHTTPURLConnectionOperation(operation);
    }

    public void PUT(String path, Map<String, Object> parameters, HTTPCompletion completion) {
    	URLRequest request = this.connectionWithMethodPathAndParameters("PUT", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.enqueueHTTPURLConnectionOperation(operation);
    }

    public void DELETE(String path, Map<String, Object> parameters, HTTPCompletion completion) {
    	URLRequest request = this.connectionWithMethodPathAndParameters("DELETE", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.enqueueHTTPURLConnectionOperation(operation);
    }

}
