package webServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Response {
	protected OutputStream outputStream;
	protected String path;
	protected String requestType;
	protected String HTTPVersion;
	protected String outputHeader = "";
	protected String bodyContent;
	protected String headersString ="";
	Response(){
		
	}
	
protected void writeToStream( OutputStream outputStream,  String bodyContent, String outputHeader ) throws IOException, InterruptedException {
		
		outputStream.write(outputHeader.getBytes());
		outputStream.write("\r\n".getBytes());
		outputStream.write(bodyContent.getBytes());
		//slowdown response
		
//		for (int i = 0; i < bodyContent.getBytes().length; i++) {
//			outputStream.write(bodyContent.getBytes(),i,1);
//			outputStream.flush();
//			Thread.sleep(10);
//		}
	}
}
