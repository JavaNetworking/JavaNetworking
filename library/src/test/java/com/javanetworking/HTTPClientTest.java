package com.javanetworking;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class HTTPClientTest {

	Charset charset = Charset.forName("UTF-8");
	
	private Map<String, Object> userParameters() {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("name", "Fritz");
		params.put("surname", "Josef");
		params.put("age", "68");
		params.put("timeOfYear", "Summer");
		
		return params;
	}
	
	@Test
	public void testSimpleParameters() {
		
		Map<String, Object> params = userParameters();
		
		String simpleQueryParams = HTTPClient.queryStringFromParametersWithCharset(params, charset);
		
		assertEquals("age=68&name=Fritz&surname=Josef&timeOfYear =Summer", simpleQueryParams);
	}
	
	@Test
	public void testSimpleJSONParameters() {
		Map<String, Object> params = userParameters();
		
		String simpleQueryParams = HTTPClient.JsonStringFromMap(params);
		
		assertEquals("", simpleQueryParams);
	}
	
	@Test
	public void testNestedParameters() {
		
		Map<String, Object> params = userParameters();
		
		HashMap<String, Object> userParams = new HashMap<String, Object>();
		userParams.put("user", params);
		
		String nestedQueryParams = HTTPClient.queryStringFromParametersWithCharset(userParams, charset);
		
		assertEquals("user[age]=68&user[name] =Fritz&user[surname] =Josef&user[timeOfYear] =Summer", nestedQueryParams);
	}
	
	@Test
	public void testNestedJSONParameters() {
		
		Map<String, Object> params = userParameters();
		
		HashMap<String, Object> userParams = new HashMap<String, Object>();
		userParams.put("user", params);
		
		String nestedQueryParams = HTTPClient.JsonStringFromMap(params);
		
		assertEquals("", nestedQueryParams);
	}
	
	@Test
	public void testMultipleNestedJSONParameters() {
		
		Map<String, Object> params = userParameters();
		
		HashMap<String, Object> userParams = new HashMap<String, Object>();
		userParams.put("user", params);
		
		List<Map<String, Object>> users = new ArrayList();
		users.add(userParams);
		users.add(userParams);
		users.add(userParams);
		users.add(userParams);
		
		HashMap<String, Object> usersParams = new HashMap<String, Object>();
		usersParams.put("users", users);
		
		String usersQueryParams = HTTPClient.JsonStringFromMap(usersParams);
		
		assertEquals("", usersQueryParams);
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
		
		String listQueryParams = HTTPClient.queryStringFromParametersWithCharset(listParams, charset);
		
		assertEquals("numbers[]=1&numbers[]=2&numbers[]=3&numbers[]=4&numbers[]=5&numbers[]=6&numbers[]=7&numbers[]=8&numbers[]=9&numbers[]=10", listQueryParams);
	}
	
	@Test
	public void testArrayJSONParameters() {
		List<Object> numbers = testArrayList();
		
		HashMap<String, Object> listParams = new HashMap<String, Object>();
		listParams.put("numbers", numbers);

		String listQueryParams = HTTPClient.JsonStringFromMap(listParams);

		assertEquals("", listQueryParams);
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
		
		String nestedSetParams = HTTPClient.queryStringFromParametersWithCharset(setParams, charset);
		
		assertEquals("numbers=3&numbers=2&numbers=10&numbers=1&numbers=7&numbers=6&numbers=5&numbers=4&numbers=9&numbers=8", nestedSetParams);
	}
	
	@Test
	public void testSetJSONParameters() {
		HashSet<String> testSet = testSet();
		
		HashMap<String, Object> setParams = new HashMap<String, Object>();
		setParams.put("numbers", testSet);
		
		String nestedSetParams = HTTPClient.JsonStringFromMap(setParams);
		
		assertEquals("", nestedSetParams);
	}
}
