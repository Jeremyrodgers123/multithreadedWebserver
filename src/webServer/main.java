package webServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.PrintWriter;

public class main {
	static HashMap<String, Room> rooms = new HashMap<String, Room>();
	
	public synchronized static Room getRoom(String roomName, Client client) {
		if(rooms.containsKey(roomName)) {
			Room newRoom = rooms.get(roomName);
			client.setRoom(newRoom);
		return newRoom;
		}else {
			Room newRoom = new Room(roomName);//create new room name
			rooms.put(roomName, newRoom);
			client.setRoom(newRoom);
			return newRoom;
		}	
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket ss = new ServerSocket(8080);
		
		while (true) {
			Socket clientSocket = ss.accept();
			System.out.println("new client accepted");
			Thread thread = new Thread(()->{
				
				try {
					Scanner inputStream = new Scanner(clientSocket.getInputStream());
					OutputStream outputStream = clientSocket.getOutputStream();
					// handle request
					try {
						Request clientRequest = new Request(inputStream);
						
						if(clientRequest.isWebSocketRequest) {
							System.out.println("This is a web socket request");
							String socketKey = clientRequest.headers.get("Sec-WebSocket-Key");
							WebSocketConnection webSocketConnection = new WebSocketConnection(outputStream,clientSocket, socketKey);
							System.out.println("socket created \r\n");
							//TODO: create new client, add socket 
							
							Client client = new Client(webSocketConnection);
							webSocketConnection.addClient(client);
							webSocketConnection.listen(client);
							System.out.println("after listen \r\n");
						
						}else {
							WebHTTPResponse serverResponse = new WebHTTPResponse(outputStream, clientRequest.requestType, clientRequest.path,
									clientRequest.version);
							serverResponse.writeResponse();
						}
						
						
						
					}catch (IOException |BadRequestException e){ 
						WebHTTPResponse serverResponse = new WebHTTPResponse(outputStream, "","", "");
						serverResponse.compose400Error();
						serverResponse.writeResponse();
					} catch (NoSuchAlgorithmException e) {
					
						e.printStackTrace();
					} catch (Exception e) {
					
						e.printStackTrace();
					}
					finally {
						System.out.println("Closing Thread due to error");
						inputStream.close();
						clientSocket.close();
					}
				}catch(IOException|InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("end of thread");
			});
			thread.start();
			
			//thread.join();

		}
	}

}
