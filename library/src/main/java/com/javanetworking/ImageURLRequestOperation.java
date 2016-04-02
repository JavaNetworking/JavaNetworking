package com.javanetworking;

import java.util.List;


/**
 {@link ImageURLRequestOperation} is a {@link HTTPURLRequestOperation} subclass for downloading images.
 
 By default {@link ImageURLRequestOperation} accepts the following MIME content types:
 
 - `image/tiff`
 - `image/jpeg`
 - `image/gif`
 - `image/png`
 - `image/ico`
 - `image/x-icon`
 - `image/bmp`
 - `image/x-bmp`
 - `image/x-xbitmap`
 - `image/x-win-bitmap`
 */
public class ImageURLRequestOperation extends HTTPURLRequestOperation {

    /**
     A static constructor method that creates and returns a {@link ImageURLRequestOperation} instance.
     */
    public static ImageURLRequestOperation operationWithURLRequest(URLRequest request, HTTPCompletion completion) {
        return new ImageURLRequestOperation(request, completion);
    }

    /**
     Instantiate this class and sets the {@link URLRequest}, and the {@link HTTPCompletion} interface.

     This is the preferred constructor.

     @param urlConnection An open {@link URLRequest} to be used for HTTP network access.
     @param completion A {@link HTTPCompletion} instance that handles the completion interface methods.
     */
    public ImageURLRequestOperation(URLRequest request, HTTPCompletion completion) {
        super(request, null);

        this.setCompletion(completion);
    }

    /**
     Get acceptable content types list for current connection. Default values for {@link ImageURLRequestOperation} is:

     - `image/tiff`
     - `image/jpeg`
     - `image/gif`
     - `image/png`
     - `image/ico`
     - `image/x-icon`
     - `image/bmp`
     - `image/x-bmp`
     - `image/x-xbitmap`
     - `image/x-win-bitmap`

     @return A {@code List<String>} with acceptable content types.
     */
    @Override
    protected List<String> getAcceptableContentTypes() {
        return HTTPURLRequestOperation.arrayToList(new String[] { "image/tiff", "image/jpeg", "image/gif", "image/png", "image/ico", "image/x-icon", "image/bmp", "image/x-bmp", "image/x-xbitmap", "image/x-win-bitmap" });
    }

}
