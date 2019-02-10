package webServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebHTTPResponse extends Response{

	
	public WebHTTPResponse(OutputStream os, String req, String p, String v ) {
		outputStream = os;
		requestType = req;
		path = p;
		HTTPVersion = v;
	}
	
	public void writeResponse() throws IOException, InterruptedException {
		if(requestType.equals("GET")) {
			editPath();
			File file = new File(path);
			if(file.exists()) {
				compose200Ok();
			}else {
				System.out.println("file Does Not exists");
				compose400Error();
			}
			
			writeToStream(outputStream, bodyContent, outputHeader);
			
		}
	}

	
	private void compose200Ok() throws IOException {
		System.out.println("file exists");
		outputHeader ="";
		bodyContent = readInFile();
		int byteCount = bodyContent.getBytes().length;
		outputHeader += "HTTP/1.1 200 OK \r\n";
		outputHeader += "Host: localhost\\r\\n";
		outputHeader += "Keep-Alive: timeout=5, max=1";
		String contentLength = "Content-Length: " + byteCount + "\r\n";
		outputHeader += contentLength;
	}
	
	public void compose400Error() throws IOException {
		
		outputHeader ="";
		outputHeader += "HTTP/1.1 400 BAD REQUEST \r\n";
		outputHeader += "Host: localhost\\r\\n";
		outputHeader += "Keep-Alive: timeout=5, max=1";
		bodyContent = "";
		String contentLength = "Content-Length: " + 0 + "\r\n";
		outputHeader += contentLength;
	}
	
	private String readInFile() throws IOException {
		String content = new String(Files.readAllBytes(Paths.get(path)));
		return content;
	}
	
	private void editPath() {
		if( path.equals("/")) {
			System.out.println("Path: " + path);
			System.out.println("file PATH CHANGED");
			path = "index.html";	
		}else {
			path = path.substring(1);
		}
	}
	
}
