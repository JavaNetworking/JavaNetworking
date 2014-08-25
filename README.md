JavaNetworking
==============

[![Build Status](https://travis-ci.org/JavaNetworking/JavaNetworking.svg?branch=master)](https://travis-ci.org/JavaNetworking/JavaNetworking)

JavaNetworking is an asynchronous java networking library.


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

### Download JSON string

```java
String urlString = "https://api.github.com/search/repositories?q=tetris+language:assembly&sort=stars&order=desc";

HttpURLConnection urlConnection;
try {
	urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
} catch (IOException e) {
	return;
}

JSONURLConnectionOperation operation = JSONURLConnectionOperation.operationWithHttpURLConnection(urlConnection, new JSONCompletion() {
	@Override
	public void failure(HttpURLConnection urlConnection, Throwable t) {
		System.out.println("Throwable: " + t);
	}
	@Override
	public void success(HttpURLConnection urlConnection, String responseData) {
		System.out.println("Response data:\n" + responseData);
	}
});
operation.start();
```

### Download image

```java
String image_url = "http://www.insidefacebook.com/wp-content/uploads/2013/01/profile-150x150.png";

HttpURLConnection connection = null;
try {
	connection = (HttpURLConnection) new URL(image_url).openConnection();
} catch (IOException e) {
	return;
}

ImageURLConnectionOperation operation = ImageURLConnectionOperation.operationWithHttpURLConnection(connection, new ImageCompletion() {
	@Override
	public void failure(HttpURLConnection urlConnection, Throwable t) {
    	System.out.println("Throwable: " + t);
	}
	@Override
	public void success(HttpURLConnection urlConnection, byte[] responseData) {
		System.out.println("Downloaded: " + responseData.length + " bytes");
	}
});
operation.start();
```

## Credits

JavaNetworking is based upon the popular iOS and OSX library [AFNetworking](http://afnetworking.com/).

## License

JavaNetworking is available under the MIT license. See the LICENSE for more info.
