package com.javanetworking;

import com.operationqueue.OperationQueue;

import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gard on 12/06/14.
 */
public class HTTPClient {

    private String baseURL;

    public enum HTTPClientParameterEncoding {
        FormURLParameterEncoding,
        JSONParameterEncoding
    }
    private HTTPClientParameterEncoding parameterEncoding;

    private Charset stringEncoding;

    private String requestBody;

    private Map<String, String> defaultHeaders;

    private OperationQueue operationQueue;

    private List<String> registeredOperationClassNames;

    public static HTTPClient clientWithBaseURL(String baseURL) {
        return new HTTPClient(baseURL);
    }


    public HTTPClient(String baseURL) {
        if (baseURL == null) {
            throw new NullPointerException("baseURL cannot be null.");
        }
        if (baseURL.isEmpty()) {
            throw new IllegalArgumentException("baseURL connot be empty");
        }
        this.baseURL = baseURL;

        stringEncoding = Charset.forName("UTF-8");
        parameterEncoding = HTTPClientParameterEncoding.FormURLParameterEncoding;

        this.registeredOperationClassNames = new ArrayList<String>();

        this.defaultHeaders = new HashMap<String, String>();


        this.setDefaultHeader("User-Agent", "JavaNetworking/0.0.5");

        this.operationQueue = new OperationQueue();
    }

    @Override
    public String toString() {
        return String.format("<HTTPClient baseURL:%s>", this.baseURL);
    }

    public boolean registerHTTPOperationClass(Class<?> operationClass) {
        if (operationClass.isAssignableFrom(HttpURLConnectionOperation.class)) {
            return false;
        }

        this.registeredOperationClassNames.set(0, operationClass.getSimpleName());

        return true;
    }

    public void setDefaultHeader(String header, String value) {
        this.defaultHeaders.put(header+":", value);
    }

    protected void setParameterEncoding(HTTPClientParameterEncoding parameterEncoding) {
        this.parameterEncoding = parameterEncoding;
    }

    public static String queryStringFromParametersWithCharset(Map<String, Object> parameters, Charset stringEncoding) {
        StringBuilder stringBuilder = new StringBuilder();
        for (QueryStringPair pair : QueryStringPairsFromMap(parameters)) {
            stringBuilder.append(pair.URLEncodedStringValueWithEncoding(stringEncoding));
        }


        return stringBuilder.toString();
    }

    public static List<QueryStringPair> QueryStringPairsFromMap(Map<String, Object> parameters) {
        return QueryStringPairsFromKeyAndValue(null, parameters);
    }

    public static List<QueryStringPair> QueryStringPairsFromKeyAndValue(String key, Object value) {
        List queryStringComponents = new ArrayList<QueryStringPair>();

        if (value instanceof Map) {
            Map map = (Map) value;

            Set<String> nestedKeys = map.keySet();
            for (String nestedKey : nestedKeys) {
                Object nestedValue = map.get(nestedKey);
                if (nestedValue != null) {
                    queryStringComponents.add(QueryStringPairsFromKeyAndValue((key != null) ? String.format("%s[%s]", key, nestedKey) : null, nestedValue));
                }
            }
        } else if (value instanceof List) {
            List<String> list = (List) value;
            for (String nestedValue : list) {
                queryStringComponents.add(QueryStringPairsFromKeyAndValue(String.format("%s[]", key), nestedValue));
            }
        } else if (value instanceof Set) {
            Set set = (Set) value;
            for (Object object : set) {
                queryStringComponents.add(QueryStringPairsFromKeyAndValue(key, object));
            }
        } else {
            queryStringComponents.add(new QueryStringPair(key, value));
        }

        return queryStringComponents;
    }


    public static String encode(String input, Charset stringEncoding) {
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
            if (this.value != null) {
                return PercentEscapedQueryStringKeyFromStringWithEncoding(field, stringEncoding);
            } else {
                return String.format("%s=%s", PercentEscapedQueryStringKeyFromStringWithEncoding(field, stringEncoding),
                        PercentEscapedQueryStringValueFromStringWithEncoding(value, stringEncoding));
            }
        }
    }

    public void enqueueHttpURLConnectionOperation(HttpURLConnectionOperation operation) {
        this.operationQueue.addOperation(operation);
    }

    public HttpURLConnection connectionWithMethodPathAndParameters(String method, String path, Map<String, Object> parameters) {

        String urlString = String.format("%s%s", this.baseURL, path);

        HttpURLConnection urlConnection = null;
        try {
            String parametersString = HTTPClient.queryStringFromParametersWithCharset(parameters, this.stringEncoding);

            if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("DELETE")) {
//                if (!urlString.substring(urlString.length() - 1).equalsIgnoreCase("?")) {
                if (!urlString.contains("?")) {
                    urlString = String.format("%s?%s", urlString, parametersString);
                } else {
                    urlString = String.format("%s&%s", urlString, parametersString);
                }
            }

            urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
            urlConnection.setRequestMethod(method);
            for (String key : this.defaultHeaders.keySet()) {
                urlConnection.setRequestProperty(key, this.defaultHeaders.get(key));
            }

            if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
                String charset = this.stringEncoding.name();

                switch (this.parameterEncoding) {
                    case FormURLParameterEncoding:
                        urlConnection.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded; charset=%s", charset));
                        this.requestBody = parametersString;
                        break;
                    case JSONParameterEncoding:
                        urlConnection.setRequestProperty("Content-Type", String.format("application/json; charset=%s", charset));
                        break;
                }
            }

            return urlConnection;
        } catch (Exception e) {
            return null;
        }
    }

    public HttpURLConnectionOperation operationWithHttpURLConnection(HttpURLConnection urlConnection, String requestBody, HttpURLConnectionOperation.HttpCompletion completion) {

        HttpURLConnectionOperation operation = null;

        for (String className : this.registeredOperationClassNames) {
            try {
                Class cl = Class.forName(className);
                Constructor constructor = cl.getConstructor(HttpURLConnection.class, String.class, HttpURLConnectionOperation.HttpCompletion.class);
                operation = (HttpURLConnectionOperation) constructor.newInstance(urlConnection, requestBody, completion);
                break;
            } catch (Exception e) {
                throw new NullPointerException(className + " class not found.");
            }
        }
        return operation;
    }

    public void GET(String path, Map<String, Object> parameters, HttpURLConnectionOperation.HttpCompletion completion) {
        HttpURLConnection urlConnection = this.connectionWithMethodPathAndParameters("GET", path, parameters);
        HttpURLConnectionOperation operation = this.operationWithHttpURLConnection(urlConnection, this.requestBody, completion);
        this.enqueueHttpURLConnectionOperation(operation);
    }
/*
    public void GET(String path, String JSONContent, HttpURLConnectionOperation.HttpCompletion completion) {

        this.parameterEncoding = HTTPClientParameterEncoding.JSONParameterEncoding;

        HttpURLConnection urlConnection = this.connectionWithMethodPathAndParameters("GET", path, null);
        HttpURLConnectionOperation operation = this.operationWithHttpURLConnection(urlConnection, JSONContent, completion);
        this.enqueueHttpURLConnectionOperation(operation);

    }
*/
    public void POST(String path, Map<String, Object> parameters, HttpURLConnectionOperation.HttpCompletion completion) {
        HttpURLConnection urlConnection = this.connectionWithMethodPathAndParameters("GET", path, parameters);
        HttpURLConnectionOperation operation = this.operationWithHttpURLConnection(urlConnection, this.requestBody, completion);
        this.enqueueHttpURLConnectionOperation(operation);
    }

    public void PUT(String path, Map<String, Object> parameters, HttpURLConnectionOperation.HttpCompletion completion) {
        HttpURLConnection urlConnection = this.connectionWithMethodPathAndParameters("GET", path, parameters);
        HttpURLConnectionOperation operation = this.operationWithHttpURLConnection(urlConnection, this.requestBody, completion);
        this.enqueueHttpURLConnectionOperation(operation);
    }

    public void DELETE(String path, Map<String, Object> parameters, HttpURLConnectionOperation.HttpCompletion completion) {
        HttpURLConnection urlConnection = this.connectionWithMethodPathAndParameters("GET", path, parameters);
        HttpURLConnectionOperation operation = this.operationWithHttpURLConnection(urlConnection, this.requestBody, completion);
        this.enqueueHttpURLConnectionOperation(operation);
    }

}
