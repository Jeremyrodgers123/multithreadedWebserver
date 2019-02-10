package webServer;

import java.util.HashMap;

public class Client {
	private WebSocketConnection _webSocket;
	 //HashMap<String, Room> roomsList;
	private Room room;
	
	Client(WebSocketConnection webSocket){
		_webSocket = webSocket;
	}
	
	public WebSocketConnection getWebSocket() {
		return _webSocket;
	}
	
	public Room addOrCreateRoom(String roomName){
		Room _room = main.getRoom(roomName, this);
		return _room;
	}
	
	public boolean hasRoom() {
		if(room != null) {
			return true;
		}
		return false;
	}
	
	public Room getRoom() {
		return room;
	}
	
	public void setRoom(Room r) {
		room = r;
	}
	
}
