JavaNetworking
==============

JavaNetworking is an asynchronous java networking library.


## Basic usage

### Build

```bash
./gradlew build
```

### Download JSon string

```java
String urlString = "https://api.github.com/search/repositories?q=tetris+language:assembly&sort=stars&order=desc";
		
URL url = new URL(urlString);
HttpURLConnection connection = (HttpURLConnection) url.openConnection();

HttpURLConnectionOperation httpOperation = HttpURLConnectionOperation.operationWithHttpURLConnection(connection, new Completion() {
	@Override
	public void failure(HttpURLConnection urlConnection, Throwable t) {
		System.out.println("Throwable: " + t);
	}
	@Override
	public void success(HttpURLConnection urlConnection, Object responseData) {
		System.out.println("Response data:\n" + responseData);
	}
});
httpOperation.start();
```


## Credits

JavaNetworking is based upon the popular iOS and OSX library [AFNetworking](http://afnetworking.com/)

## License

JavaNetworking is available under the MIT license. See the LICENSE for more info.