JavaNetworking
==============

[![Build Status](https://travis-ci.org/JavaNetworking/JavaNetworking.svg?branch=master)](https://travis-ci.org/JavaNetworking/JavaNetworking)

JavaNetworking is an asynchronous java networking library.


## Basic usage

### Build

#### Windows
```cmd
gradlew.bat build
```

#### Unix
```bash
./gradlew build
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

JSONURLConnectionOperation httpOperation = JSONURLConnectionOperation.operationWithHttpURLConnection(urlConnection, new JSONCompletion() {
    @Override
    public void failure(HttpURLConnection urlConnection, Throwable t) {
        System.out.println("Throwable: " + t);
    }
    @Override
    public void success(HttpURLConnection urlConnection, String responseData) {
        System.out.println("Response data:\n" + responseData);
    }
});
httpOperation.start();
```


## Credits

JavaNetworking is based upon the popular iOS and OSX library [AFNetworking](http://afnetworking.com/).

## License

JavaNetworking is available under the MIT license. See the LICENSE for more info.
