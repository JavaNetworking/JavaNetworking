JavaNetworking
==============

[![Build Status](https://travis-ci.org/JavaNetworking/JavaNetworking.svg?branch=master)](https://travis-ci.org/JavaNetworking/JavaNetworking)

JavaNetworking is a delightful Java networking library.


## Basic usage

### Build

##### Windows
```cmd
gradlew.bat build
```

##### Unix
```bash
./gradlew build
```

##### Built JAR path
```
JavaNetworking/library/build/libs/JavaNetworking-*.*.*.jar
```

### Run tests

##### Windows
```cmd
gradlew.bat test
```

##### Unix
```bash
./gradlew test
```

### Download JSON

```java
String urlString = "https://api.github.com/search/repositories?q=tetris+language:assembly&sort=stars&order=desc";

URLRequest request = URLRequest.requestWithURLString(urlString);

JSONURLRequestOperation operation = JSONURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
    @Override
    public void failure(URLRequest request, Throwable t) {
        System.out.println("Throwable: " + t);
    }
    @Override
    public void success(URLRequest request, Object responseData) {
        System.out.println("Response data:\n" + responseData);
    }
});
operation.start();
```
Response is a 'com.javanetworking.gson.JsonElement' object created from the JSON response.


### Download XML

```java
String urlString = "http://httpbin.org/xml";
        
URLRequest request = URLRequest.requestWithURLString(urlString);

XMLURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
    @Override
    public void failure(URLRequest request, Throwable t) {
        System.out.println("Throwable: " + t);
    }
    @Override
    public void success(URLRequest request, Object response) {
        System.out.println("Response data:\n" + response);
    }
}).start();
```
Response is a 'org.w3c.dom.Document' object created from the XML response.


### Download image

```java
String image_url = "http://www.insidefacebook.com/wp-content/uploads/2013/01/profile-150x150.png";

URLRequest request = URLRequest.requestWithURLString(image_url);

ImageURLRequestOperation operation = ImageURLRequestOperation.operationWithURLRequest(request, new HTTPCompletion() {
	@Override
	public void failure(URLRequest request, Throwable t) {
    	System.out.println("Throwable: " + t);
	}
	@Override
	public void success(URLRequest request, Object responseData) {
		System.out.println("Downloaded: " + ((byte[])responseData).length + " bytes");
	}
});
operation.start();
```

### TODO
* File upload
* Progress callback
* Redirect callback
* Android example
* Java example


## Credits

JavaNetworking is based upon the popular iOS and OSX library [AFNetworking](http://afnetworking.com/).

## License

JavaNetworking is available under the MIT license. See the LICENSE for more info.
