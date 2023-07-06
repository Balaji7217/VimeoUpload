package com.teamlease;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import com.clickntap.vimeo.Vimeo;
import com.clickntap.vimeo.VimeoResponse;

public class VimeoUpload {
	
	 public static void main(String[] args) throws Exception {
		    Vimeo vimeo = new Vimeo("0756caf7e65c4b4028c9422629ebd368"); 
		  
		    //Command line arugumentsuser input
		   // String VIDEO_PATH = args[0];
		    
		    Scanner scanner = new Scanner(System.in);
	        System.out.print("Enter the directory path: ");
	        String VIDEO_PATH = scanner.nextLine();
	        scanner.close();
	        
		    final int BATCH_SIZE = 1;
		//final String VIDEO_PATH = "C:\\Users\\balaj\\Downloads\\VideosVimeo\\";
              
		    File videoDirectory = new File(VIDEO_PATH);
			File[] videoFiles = videoDirectory.listFiles();
			 List<String> uploadedVideos = new ArrayList<>();
			
			 
			  
    Set<String> uploadedVideoIdentifiers = loadExistingVideoIdentifiers();
     System.out.println("uploadedVideoIdentifiers"+uploadedVideoIdentifiers);
		
   int uploadCount = 0;
   int startIndex = 0;
   
   // Load the progress from the progress file
      Map<String, Integer> uploadProgress = loadUploadProgress();
   

//      if (!uploadProgress.isEmpty()) {
//          startIndex = uploadProgress.size();
//          System.out.println("Resuming upload from index: " + startIndex);
//      }
      
      // Upload new videos
      LocalTime currentTime = LocalTime.now();
      boolean isWithinUploadWindow = currentTime.isAfter(LocalTime.of(21, 0)) || currentTime.isBefore(LocalTime.of(9, 0));
        
        //upload new videos
     // if (isWithinUploadWindow) {
     for (int i = startIndex; i < Math.min( videoFiles.length,startIndex + BATCH_SIZE); i++) {
  
	   File videoFile = videoFiles[i];
	   String videoName = videoFile.getName();
	   
	   String videoIdentifier = generateVideoIdentifier(videoFile);
     
      // Check if the video is already uploaded
	   if (uploadedVideoIdentifiers.contains(videoIdentifier)) {
           System.out.println("Skipping upload for existing video: " + videoName);
           continue;
       }
	   
	    // Check if the video was partially uploaded
       if (uploadProgress.containsKey(videoIdentifier)) {
           int progress = uploadProgress.get(videoIdentifier);
           System.out.println("Resuming upload for video: " + videoName + " at progress: " + progress);

           // Skip the already uploaded portion
           continueUpload(vimeo, videoFile, progress);

           // Add the video to the uploaded list and identifiers
           uploadedVideos.add(videoName);
           uploadedVideoIdentifiers.add(videoIdentifier);

           continue;
       }
	   
	 //upload video
 	  String videoEndPoint =   vimeo.addVideo(videoFile);
 	  VimeoResponse info = vimeo.getVideoInfo(videoEndPoint);
 	  
 	  Properties props = new Properties();
	  props.put("id", info.getJson().getString("uri"));
	  

	  String path1 = "C:\\Users\\balaj\\Downloads\\myfile.properties";
	  FileOutputStream outputStrem = new FileOutputStream(path1);
	  props.store(outputStrem, "This is properties file");
	  System.out.println("Properties o/p "+ outputStrem );
      System.out.println("Properties file created......");
      
      
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
	    
	    // Add the video to the uploaded list and identifiers
        uploadedVideos.add(videoName);
	 	uploadedVideoIdentifiers.add(videoIdentifier);
		saveExistingVideoIdentifiers(uploadedVideoIdentifiers);
		
		// Store the progress in the progress file
        uploadProgress.put(videoIdentifier, 100);
        saveUploadProgress(uploadProgress);

        uploadCount++;
     }
   
   System.out.println("Upload completed. Uploaded videos: " + uploadedVideos.size());
   //}
//      else {
//          System.out.println("Videos won't be upload between 9AM to 9PM. No new videos will be uploaded.");
//      }
	 }
	 
	
	 
	 private static String generateVideoIdentifier(File videoFile) {
	      // Generate a unique identifier for the video file based on its properties
	      String videoName = videoFile.getName();
	      long videoSize = videoFile.length();
	      long videoLastModified = videoFile.lastModified();
	      // Concatenate the properties to form the identifier
	      return videoName + "_" + videoSize + "_" + videoLastModified;
	  }
	 
	 private static Set<String> loadExistingVideoIdentifiers() {
		    Set<String> identifiers = new HashSet<>();
		    try (FileInputStream inputStream = new FileInputStream("C:\\Users\\balaj\\Downloads\\myfile.properties")) {
		        Properties properties = new Properties();
		        properties.load(inputStream);
		        for (String key : properties.stringPropertyNames()) {
		            identifiers.add(properties.getProperty(key));
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    return identifiers;
		}
	 
	 private static void saveExistingVideoIdentifiers(Set<String> identifiers) {
		    Properties properties = new Properties();
		    int index = 0;
		    for (String identifier : identifiers) {
		        properties.setProperty(String.valueOf(index++), identifier);
		    }
		    try (FileOutputStream outputStream = new FileOutputStream("C:\\Users\\balaj\\Downloads\\myfile.properties")) {
		        properties.store(outputStream, "Video Identifiers");
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	 private static void continueUpload(Vimeo vimeo, File videoFile, int progress) {
	        // Implement logic to resume the upload from the specified progress
		 try (FileInputStream inputStream = new FileInputStream(videoFile)) {
			  long totalFileSize = videoFile.length();
		        long remainingFileSize = totalFileSize - progress;
		        
		        
		        byte[] chunkBuffer = new byte[4096]; // Adjust the chunk size as needed
		       
		        // Skip to the appropriate position in the video file
		        inputStream.skip(progress);


		        // Upload the video chunk and monitor the progress
		        while (remainingFileSize > 0) {
		            int bytesRead = inputStream.read(chunkBuffer, 0, (int) Math.min(remainingFileSize, chunkBuffer.length));

		            // Upload the chunk and get the response
		           // VimeoResponse response = vimeo.uploadVideoChunk(chunkBuffer, bytesRead);

		            // Update the progress value based on the uploaded chunk length
		            progress += bytesRead;

		            // Monitor the progress and handle the response
		            // ...

		            remainingFileSize -= bytesRead;
		        }
		        System.out.println("Upload completed for file: " + videoFile.getName());
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	 
	 }
	 
	 private static Map<String, Integer> loadUploadProgress() {
	        Map<String, Integer> uploadProgress = new HashMap<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\balaj\\Downloads\\FileInfo.txt"))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                String[] parts = line.split(":");
	                if (parts.length == 2) {
	                    String videoIdentifier = parts[0];
	                    int progress = Integer.parseInt(parts[1]);
	                    uploadProgress.put(videoIdentifier, progress);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return uploadProgress;
	 }
	 
	 private static void saveUploadProgress(Map<String, Integer> uploadProgress) {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\balaj\\Downloads\\FileInfo.txt"))) {
	            for (Map.Entry<String, Integer> entry : uploadProgress.entrySet()) {
	                String line = entry.getKey() + ":" + entry.getValue();
	                writer.write(line);
	                writer.newLine();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	 
}
