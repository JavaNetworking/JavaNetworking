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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
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
     A boolean value indicating if the HTTPClient should be asynchronous. Default is true.
     */
    private boolean asynchronous = true;

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

    /**
     Get the query string parameter {@link Charset} encoding.
     */
    public Charset getStringEncoding() {
		return this.stringEncoding;
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

    /**
     Add default header value to default headers.

     @param header A string value representing the header name.
     @param value A string value representing the header field value.
     */
    public void setDefaultHeader(String header, String value) {
        this.defaultHeaders.put(header, value);
    }

    public void setAuthorizationHeaderWithUsernameAndPassword(String username, String password) {
		String basicAuthCredentials = String.format("%s:%s", username, password);
		this.setDefaultHeader("Authorization", String.format("Basic %s", Base64EncodedStringFromString(basicAuthCredentials)));
    }

    /**
     Set the {@link HTTPClientParameterEncoding} parameter encoding value.

     @param parameterEncoding A {@link HTTPClientParameterEncoding} value indicating the encoding to be used.
     */
    public void setParameterEncoding(HTTPClientParameterEncoding parameterEncoding) {
        this.parameterEncoding = parameterEncoding;
    }

    /**
     Creates a query string from parameters with given {@link Charset} encoding.

     @param parameters A {@link Map} object with string keys and object values.
     @param stringEncoding A {@link Charset} value representing the wanted string encoding.

	 @return A query string from parameters with desired string encoding.
     */
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

    /**
     Return a {@link List} of {@link QueryStringPair}s from a {@link Map}.

     @param parameters A {@link Map} object with string keys and object values.

     @return A {@link List} of {@link QueryStringPair} objects.
     */
    public static List<QueryStringPair> QueryStringPairsFromMap(Map<String, Object> parameters) {
        return QueryStringPairsFromKeyAndValue(null, parameters);
    }

    /**
	 Return a {@link List} of {@link QueryStringPair}s from a string value key and object value.
	 
	 @param key A string value representing the key value in the query parameter.
	 @param valey An object value representing the query parameter value.

	 @return A {@link List} of {@link QueryStringPair} objects generated from key and value.
     */
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

    /**
	 Return a JSON string from a {@link Map} object.

	 @param parameters A {@link Map} of the query parameters. 

	 @return A JSON string generated from query parameters. 
     */
	public static String JsonStringFromMap(Map<String, Object> parameters) {
		return new Gson().toJson(parameters);
    }

	/**
	 Get Base64 encoded string from input string.

	 @param string The string to be encoded with Base64-encoding.

	 @return A Base64 encoded string representation of parameter string.
	 */
	public static String Base64EncodedStringFromString(String string) {
		byte[] data = string.getBytes();
        int length = data.length;

        byte[] input = data;
        byte[] output = new byte[((length + 2) / 3) * 4];

        for (int i = 0; i < length; i += 3) {
            int value = 0;
            for (int j = i; j < (i + 3); j++) {
                value <<= 8;
                if (j < length) {
                    value |= (0xFF & input[j]);
                }
            }

            String kBase64EncodingTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

            int idx = (i / 3) * 4;
            output[idx + 0] = (byte) kBase64EncodingTable.charAt((value >> 18) & 0x3F);
            output[idx + 1] = (byte) kBase64EncodingTable.charAt((value >> 12) & 0x3F);
            output[idx + 2] = (byte) ((i + 1) < length ? kBase64EncodingTable.charAt((value >> 6)  & 0x3F) : '=');
            output[idx + 3] = (byte) ((i + 2) < length ? kBase64EncodingTable.charAt((value >> 0)  & 0x3F) : '=');
        }

        return new String(output);
    }

	/**
	 Encode an input string with the desired string encoding.

	 @param input The string input to be encoded. 
	 @param stringEncoding The {@link Charset} value to encode the string by.
	 */
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

    /**
     Inner class representing a query string pair.
     */
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

    /**
     Sets the asynchronous boolean indicating if HTTPClient should be asynchronous or synchronous. Default is true.

     @param asynchronous A boolean value indicating if the clients requests should be asynchronous.
     */
    public void setAsynchronous(boolean asynchronous) {
		this.asynchronous = asynchronous;
    }

    public void enqueueHTTPURLRequestOperation(HTTPURLRequestOperation operation) {
    	this.operationQueue.addOperation(operation);
    }

    public void prepareHTTPURLRequestOperationForExecution(HTTPURLRequestOperation operation) {
    	if (asynchronous) {
    		this.enqueueHTTPURLRequestOperation(operation);
		} else {
			operation.startSynchronous();
		}
    }

    /**
     Create a {@link URLRequest} connection with method, path and parameters.

	 @param method The HTTP method to be used.
	 @param path The resource path of the {@link URLReqeust}.
	 @param parameters The query string parameters.

	 @return A {@link URLRequest} connection to be used by an operation.
     */
    public URLRequest requestWithMethodPathAndParameters(String method, String path, Map<String, Object> parameters) {
    	if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
        String urlString = String.format("%s%s", this.baseURL, path);
        
        // Add GET/HEAD/DELETE parameters to URL string
        if (parameters != null && (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("DELETE"))) {
			urlString = String.format("%s%c%s", urlString, (urlString.contains("?") ? '&' : '?'), HTTPClient.queryStringFromParametersWithCharset(parameters, getStringEncoding()));
        }
        
        URLRequest request = URLRequest.requestWithURLString(urlString);
        request.setRequestMethod(method);
        request.setConnectTimeout(500);
        for (String key : this.defaultHeaders.keySet()) {
        	request.setRequestProperty(key, this.defaultHeaders.get(key));
        }
        
        // Set POST/PUT requestBody on operation 
        if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
			String charsetName = getStringEncoding().name();

            switch (this.parameterEncoding) {
                case FormURLParameterEncoding:
					request.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded; charset=%s", charsetName));
					request.setHTTPBody(HTTPClient.queryStringFromParametersWithCharset(parameters, getStringEncoding()).getBytes(getStringEncoding()));
                    break;
                case JSONParameterEncoding:
					request.setRequestProperty("Content-Type", String.format("application/json; charset=%s", charsetName));
                    request.setHTTPBody(HTTPClient.JsonStringFromMap(parameters).getBytes(getStringEncoding()));
                    break;
            }
        }

        return request;
    }

    
    //
    // Multipart form request
    //
    
    public interface MultipartFormInterface {
    	void multipartFormData(MultipartFormData formData);
    }
    
    public URLRequest multipartFormRequestWithMethodPathParametersAndInterface(String method, String path, Map<String, Object> parameters, final MultipartFormInterface multipartInterface) {
    	
    	URLRequest request = this.requestWithMethodPathAndParameters(method, path, null);
    	
    	StreamingMultipartFormData formData = StreamingMultipartFormData.multipartFormDataWithRequestAndStringEncoding(request, this.stringEncoding);
    	
    	if (parameters != null) {
            for (QueryStringPair pair : QueryStringPairsFromMap(parameters)) {
            	byte[] data = null;
            	
            	if (pair.value != null) {
            		data = pair.value.getBytes(this.stringEncoding);
            	}
            	
            	if (data != null) {
            		formData.appendPartWithFormDataAndName(data, pair.field);
            	}
            }
        }

        if (multipartInterface != null) {
        	multipartInterface.multipartFormData(formData);
        }
    	
    	return formData.requestByFinalizingMultipartFormData();
    }

    public static class HTTPBodyPart {
    	private Charset stringEncoding;
    	private Map<String, String> headers;
    	private byte[] body;
    	private int bodyContentLength;
    	private InputStream inputStream;
    	private boolean hasInitialBoundary;
    	private boolean hasFinalBoundary;
    	
    	
    	public HTTPBodyPart() {
    		this.transitionToNextPhase();
    	}
    	
    	public InputStream getInputStream() {
    		if (inputStream == null) {
				inputStream = new ByteArrayInputStream(this.body, 0, this.body.length);
			}
    		return inputStream;
    	}
    	
    	public boolean hasBytesAvailable() throws IOException {
    		return (this.inputStream.available() > 0);
    	}
    	
    	public int read(byte[] buffer) {
    		
    		
			return 0;
    	}
    }
    
    public static class MultipartBodyStream extends InputStream {

    	private Charset stringEncoding;
    	private List<HTTPBodyPart> HTTPBodyParts;
    	private int numberOfBytesInPacket;
		public int contentLength;
    	private HTTPBodyPart currentHTTPBodyPart;
		
		
    	public MultipartBodyStream(Charset encoding) {

    		this.stringEncoding = encoding;
    		this.HTTPBodyParts = new ArrayList<HTTPClient.HTTPBodyPart>();
    		this.numberOfBytesInPacket = Integer.MAX_VALUE;

    	}
    	
    	@Override
    	public int read(byte[] b, int off, int length) throws IOException {
    		
    		int totalNumberOfBytesRead = 0;

		    while ((int)totalNumberOfBytesRead < Math.min(length, this.numberOfBytesInPacket)) {
		        if (this.currentHTTPBodyPart == null || !this.currentHTTPBodyPart.hasBytesAvailable()) {
		            if (!(this.currentHTTPBodyPart = [self.HTTPBodyPartEnumerator nextObject])) {
		                break;
		            }
		        } else {
		            int maxLength = length - (int)totalNumberOfBytesRead;
		            int numberOfBytesRead = [self.currentHTTPBodyPart read:&buffer[totalNumberOfBytesRead] maxLength:maxLength];
		            if (numberOfBytesRead == -1) {
		                self.streamError = self.currentHTTPBodyPart.inputStream.streamError;
		                break;
		            } else {
		                totalNumberOfBytesRead += numberOfBytesRead;

		                if (self.delay > 0.0f) {
		                    [NSThread sleepForTimeInterval:self.delay];
		                }
		            }
		        }
		    }
		    
		    return totalNumberOfBytesRead;
    	}

		public void setInitialAndFinalBoundaries() {
			if (this.HTTPBodyParts.size() > 0) {
		        for (HTTPBodyPart bodyPart : this.HTTPBodyParts) {
		            bodyPart.hasInitialBoundary = false;
		            bodyPart.hasFinalBoundary = false;
		        }

		        this.HTTPBodyParts.get(0).hasInitialBoundary = true;
		        this.HTTPBodyParts.get(this.HTTPBodyParts.size()-1).hasFinalBoundary = true;
		    }
		}

		public boolean isEmpty() {
			return this.HTTPBodyParts.isEmpty();
		}

		public void appendHTTPBodyPart(HTTPBodyPart bodyPart) {
			this.HTTPBodyParts.add(bodyPart);
		}
    }
    
    public interface MultipartFormData {
		boolean appendPartWithFileURLAndName(URL fileUrl, String name);
		boolean appendPartWithFileURLNameFilenameAndMimetype(URL fileUrl, String name, String filename, String mimeType);
		void appendPartWithInputStreamNameFilenameAndLength(InputStream inputStream, String name, String filename, int length, String mimetype);
		void appendPartWithFileDataNameFilenameAndMimetype(byte[] data, String name, String filename, String mimetype);
		void appendPartWithFormDataAndName(byte[] data, String name);
		void appendPartWithHeadersAndBody(Map<String, String> headers, byte[] body);
	}

    public static class StreamingMultipartFormData implements MultipartFormData {

    	private URLRequest request;
    	private Charset stringEncoding;
    	
    	private MultipartBodyStream bodyStream;
    	
    	private String boundary;
    	
		public StreamingMultipartFormData(URLRequest request, Charset stringEncoding) {
			this.request = request;
			this.stringEncoding = stringEncoding;
			this.boundary = String.format("Boundary+%08X%08X", Math.random(), Math.random());
			this.bodyStream = new MultipartBodyStream(stringEncoding);
		}

		public static StreamingMultipartFormData multipartFormDataWithRequestAndStringEncoding(URLRequest request, Charset stringEncoding) {
			return new StreamingMultipartFormData(request, stringEncoding);
		}

		@Override
		public boolean appendPartWithFileURLAndName(URL fileUrl, String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean appendPartWithFileURLNameFilenameAndMimetype(
				URL fileUrl, String name, String filename, String mimeType) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void appendPartWithInputStreamNameFilenameAndLength(
				InputStream inputStream, String name, String filename,
				int length, String mimetype) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void appendPartWithFileDataNameFilenameAndMimetype(byte[] data,
				String name, String filename, String mimetype) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void appendPartWithFormDataAndName(byte[] data, String name) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Disposition", String.format("form-data; name=\"%s\"", name));

			this.appendPartWithHeadersAndBody(headers, data);
		}

		@Override
		public void appendPartWithHeadersAndBody(Map<String, String> headers, byte[] body) {
			
			HTTPBodyPart bodyPart = new HTTPBodyPart();
		    bodyPart.stringEncoding = this.stringEncoding;
		    bodyPart.headers = headers;
		    bodyPart.boundary = this.boundary;
		    bodyPart.bodyContentLength = body.length;
		    bodyPart.body = body;

		    this.bodyStream.appendHTTPBodyPart(bodyPart);
		}
		
		public URLRequest requestByFinalizingMultipartFormData() {
			if (this.bodyStream.isEmpty()) {
		        return this.request;
		    }
			
			this.bodyStream.setInitialAndFinalBoundaries();
			
			this.request.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", this.boundary));
			this.request.setRequestProperty("Content-Length", String.format("%d", this.bodyStream.contentLength));
			this.request.setHTTPBodyStream(this.bodyStream);
			
			return this.request;
		}
    }
    
    
    //
    // Multipart form reqeust end
    //
    
    
    
    /**
     Creates a {@link HTTPURLRequestOperation} with an {@link URLRequest} and a {@link HTTPCompletion} callback.

	 @param request The {@link URLRequest} connection to be used in this operation. 
	 @param completion The {@link HTTPCompletion} callback method that return the response of the operation.

	 @return A {@link HTTPURLRequestOperation} based on the {@code registeredOperationClassNames}.
     */
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

    /**
     Creates an {@link HTTPURLRequestOperation} with a `GET` request. By default the client is asynchronous
     and the operation is enqueued on the clients {@link OperationQueue}.

     @param path The path to be appended to the HTTP client's base URL and used as the request URL.
     @param parameters The parameters to be encoded and appended as the query string for the request URL.
     @param completion A callback object that is called when the request operation finishes.
     */
    public void GET(String path, Map<String, Object> parameters, HTTPCompletion completion) {
        URLRequest request = this.requestWithMethodPathAndParameters("GET", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.prepareHTTPURLRequestOperationForExecution(operation);
    }

    /**
     Creates an {@link HTTPURLRequestOperation} with a `POST` request. By default the client is asynchronous
     and the operation is enqueued on the clients {@link OperationQueue}.

     @param path The path to be appended to the HTTP client's base URL and used as the request URL.
     @param parameters The parameters to be encoded and appended as the query string for the request URL.
     @param completion A callback object that is called when the request operation finishes.
     */
    public void POST(String path, Map<String, Object> parameters, HTTPCompletion completion) {
    	URLRequest request = this.requestWithMethodPathAndParameters("POST", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.prepareHTTPURLRequestOperationForExecution(operation);
    }

    /**
     Creates an {@link HTTPURLRequestOperation} with a `PUT` request. By default the client is asynchronous
     and the operation is enqueued on the clients {@link OperationQueue}.

     @param path The path to be appended to the HTTP client's base URL and used as the request URL.
     @param parameters The parameters to be encoded and appended as the query string for the request URL.
     @param completion A callback object that is called when the request operation finishes.
     */
    public void PUT(String path, Map<String, Object> parameters, HTTPCompletion completion) {
    	URLRequest request = this.requestWithMethodPathAndParameters("PUT", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.prepareHTTPURLRequestOperationForExecution(operation);
    }

    /**
     Creates an {@link HTTPURLRequestOperation} with a `PATCH` request. By default the client is asynchronous
     and the operation is enqueued on the clients {@link OperationQueue}.

     @param path The path to be appended to the HTTP client's base URL and used as the request URL.
     @param parameters The parameters to be encoded and appended as the query string for the request URL.
     @param completion A callback object that is called when the request operation finishes.
     */
    public void PATCH(String path, Map<String, Object> parameters, HTTPCompletion completion) {
		URLRequest request = this.requestWithMethodPathAndParameters("PATCH", path, parameters);
		HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
		this.prepareHTTPURLRequestOperationForExecution(operation);
   	}

    /**
     Creates an {@link HTTPURLRequestOperation} with a `DELETE` request. By default the client is asynchronous
     and the operation is enqueued on the clients {@link OperationQueue}.

     @param path The path to be appended to the HTTP client's base URL and used as the request URL.
     @param parameters The parameters to be encoded and appended as the query string for the request URL.
     @param completion A callback object that is called when the request operation finishes.
     */
    public void DELETE(String path, Map<String, Object> parameters, HTTPCompletion completion) {
    	URLRequest request = this.requestWithMethodPathAndParameters("DELETE", path, parameters);
        HTTPURLRequestOperation operation = this.operationWithURLRequest(request, completion);
        this.prepareHTTPURLRequestOperationForExecution(operation);
    }

}
