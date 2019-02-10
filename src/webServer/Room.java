package webServer;

import java.io.IOException;
import java.util.ArrayList;

public class Room {
	public String name = "";
	private ArrayList<Client> clients;
	
	Room(String n){
		name = n;
		clients = new ArrayList<Client>();
	}
	
	public synchronized void addClient(Client c) {
		clients.add(c);
	}
	
	public synchronized void postMessage(String message) throws BadRequestException, IOException {
		for(Client client: clients) {
			client.getWebSocket().writeMessage("text", message);
		}
	}
	public synchronized void removeClient(Client c) {
		clients.remove(c);
	}
}
