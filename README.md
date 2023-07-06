### Welcome to Java Vimeo API 3.4.
To use this api you’ll first need to register your app from Vimeo:

https://developer.vimeo.com/apps

Then you'll need to generate an Access Token with upload access.
The generated Token is all you need to use the Java Vimeo API 3.x.

```java

package com.clickntap.vimeo;

import java.io.File;

public class VimeoSample {

  public static void main(String[] args) throws Exception {
    Vimeo vimeo = new Vimeo("[token]"); 
    
    //add a video
    String videoEndPoint = vimeo.addVideo(new File("/Users/tmendici/Downloads/Video.AVI"));
    
    //get video info
    VimeoResponse info = vimeo.getVideoInfo(videoEndPoint);
    System.out.println(info);
    
    //edit video
    String name = "Name";
    String desc = "Description";
    String license = ""; //see Vimeo API Documentation
    String privacyView = "disable"; //see Vimeo API Documentation
    String privacyEmbed = "whitelist"; //see Vimeo API Documentation
    boolean reviewLink = false;
    vimeo.updateVideoMetadata(videoEndPoint, name, desc, license, privacyView, privacyEmbed, reviewLink);
    
    //add video privacy domain
    vimeo.addVideoPrivacyDomain(videoEndPoint, "clickntap.com");
   
    //delete video
    vimeo.removeVideo(videoEndPoint);
    
  }

}


```

The class VideoResponse provides response code and json response, see Vimeo API documentation to check errors.

### Use with Maven

```xml

<dependency>
  <groupId>com.clickntap</groupId>
  <artifactId>vimeo</artifactId>
  <version>2.0</version>
</dependency>

```

Functionality includes : 
1.Upload videos only between say 9PM to 9AM (when least consumption is expected for existing videos)
2.Video path will be taken from user input/Command line argument
3.Maximum video to be upload as 1 - which  can be modify  in batch size based on our need
4.Existing videos  can be track in property file
5. Make sure you should give property file path based on your local 
6.we can test in Java 11 version

### Support or Contact
Having trouble with Java Vimeo API 3.4? Contact info@clickntap.com and we’ll help you sort it out.
