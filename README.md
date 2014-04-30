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
		
HttpURLConnection urlConnection;
try {
    urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
} catch (IOException e) {
    return;
}

HttpURLConnectionOperation httpOperation = HttpURLConnectionOperation.operationWithHttpURLConnection(urlConnection, new Completion() {
	@Override
	public void failure(HttpURLConnection urlConnection, Throwable t) {
		System.out.println("Throwable: " + t);
	}
	@Override
	public void success(HttpURLConnection urlConnection, byte[] responseData) {
		System.out.println("Response data:\n" + new String(responseData));
	}
});
httpOperation.start();
```


## Credits

JavaNetworking is based upon the popular iOS and OSX library [AFNetworking](http://afnetworking.com/).

## License

JavaNetworking is available under the MIT license. See the LICENSE for more info.