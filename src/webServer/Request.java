package webServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Request {
	String requestType;
	String path;
	String version;
	HashMap<String, String> headers;
	Scanner inputStream;
	Boolean isWebSocketRequest = false;
	
	public Request(Scanner sc) throws BadRequestException{
		inputStream = sc;
		readRequest();
	}
	
	private void readRequest() throws BadRequestException {
	    headers = new HashMap <String, String>();
	
    	requestType = inputStream.next();
    	//System.out.println("GET : " + requestType);
    	if(!requestType.equals("GET")) {
    		System.out.println("Bad GET about to throw Bad request");
    		throw new BadRequestException();
    	}
		path = inputStream.next();
		version = inputStream.next();
		
		if(!version.equals("HTTP/1.1")) {
			System.out.println("Bad HTTP about to throw Bad request");
    		throw new BadRequestException();
    	}
		inputStream.nextLine();
	    
		
		//System.out.println(requestType +" " + path +" " + version);

		while(true) {
			String nextLine = inputStream.nextLine();
			int length = nextLine.length();
			if(length == 0) {
				break;
			}
			String [] stringArr = nextLine.split(": ", 2);
			//System.out.println("stringArr " + "key: " +stringArr[0] + " "+ "value: "+  stringArr[1]);
			headers.put(stringArr[0], stringArr[1]);
		}
		checkIfWebSocketRequest();
		//printHeaders();
	}
	
	public void checkIfWebSocketRequest(){
		
		if (headers.containsKey("Sec-WebSocket-Key") && headers.containsKey("Upgrade") && headers.containsKey("Sec-WebSocket-Version") ) {
			isWebSocketRequest = true;
		}
	}
	
}
