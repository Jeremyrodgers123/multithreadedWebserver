package webServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;

public class WebSocketConnection extends Response{
//	
	String response = "";
	HashMap<String, String> headers;
	String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	String socketKey = "";
	public String magicKey = "";
	String hashedString;//for testing
	String serverMessage = "hello";
	double max16Bits = Math.pow(2, 15)-1;
	double max64Bits = Math.pow(2, 63)-1;
	InputStream inputStream;
	Client client;
	Socket clientSocket;
	
	WebSocketConnection(OutputStream os, Socket _clientSocket, String sk) throws NoSuchAlgorithmException, IOException, InterruptedException{
		socketKey = sk;
		outputStream = os;
		inputStream = _clientSocket.getInputStream();
		headers = new HashMap<String,String>();
		clientSocket = _clientSocket;
		handshake();
	}
	
	public void addClient(Client c){
		client = c;
	}
	/**************************
	 * 
	 * HANDSHAKE RELATED CODE
	 * 
	 * ************************/
	private void handshake() throws NoSuchAlgorithmException, IOException, InterruptedException {
		outputHeader = "HTTP/1.1 101 Switching Protocols \r\n";
		headers.put("Upgrade", "websocket");
		headers.put("Connection", "Upgrade");
		headers.put("Sec-WebSocket-Accept", acceptKey());
		//send response
		buildHeadersString();
		//System.out.println(outputHeader);
		writeToStream(outputStream,"", outputHeader);
	}
	
	private String acceptKey() throws NoSuchAlgorithmException {
		magicKey = socketKey+magicString;
		//System.out.println("magicKey: " + magicKey);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte [] mdBytes = md.digest(magicKey.getBytes());
		String encodedString = Base64.getEncoder().encodeToString(mdBytes);
		hashedString = encodedString;
		return encodedString;
	}
	
	private void buildHeadersString() {
		headers.forEach((key, value)->{
			outputHeader += key + ": " + value +"\r\n";
		});
	};
	
	public String getHashedString() {
		return hashedString;
	}
	
	

	/**************************
	 * 
	 * WRITE/LISTEN
	 * 
	 * ************************/
	

	public synchronized void  writeMessage(String t, String m ) throws BadRequestException, IOException {
		buildFrame(t, m);
	}
	
	public void listen( Client client) throws Exception, BadMessageException {
		while(true) {
			System.out.println("\r\nSTART LISTENING \r\n");
			int firstByte = inputStream.read();
			String message = read(firstByte);
			System.out.println("message in listen:" + message);
			try {
				handleMessage(message);
			}catch (BadMessageException e){
				//check if username exists
				if(client!= null && client.hasRoom()) {
					Room currentRoom = client.getRoom();
					currentRoom.removeClient(client);
					System.out.println("Removed client from room");
				}
				throw new BadRequestException();
				//if yes, get room
				//remove user from room. 
				//throw error
			}
			
			System.out.println("\r\nLISTEN LOOP ITERATION \r\n");
		}
	}
	
	/**************************
	 * 
	 * READING RELATED RELATED CODE
	 * @throws Exception 
	 * 
	 * ************************/
	
	private void handleMessage(String message) throws Exception, BadMessageException {
		//join or create room
		
		System.out.println("I made it past exception");
		String words[]; 
		String firstWord; 
		String secondWord;
		try {
		  words = message.split(" ", 2);
		  firstWord = words[0]; 
		  secondWord = words[1];
		}catch(ArrayIndexOutOfBoundsException e) {
			throw new BadMessageException();
		}
		System.out.println("firstWord:" + words[0]);
		System.out.println("secondWord:" + words[1]);
		if(!client.hasRoom()) {	
			if(firstWord.toLowerCase().equals("join")) {
				if (secondWord != "" || secondWord!= null ) {
					joinNMessageRoom(secondWord);
				}
			}else {
				throw new Exception("need to join a room");
			}
		}else {
			echoMessage(words);
		}
		
	}
	private void joinNMessageRoom(String roomName) throws BadRequestException, IOException {
		Room room = client.addOrCreateRoom(roomName);
		room.addClient(client);
		String jsons = stringToJSON("Room", "user joined room");
		client.getRoom().postMessage(jsons);		
	}
	
	private void echoMessage(String words []) throws BadRequestException, IOException {
		String firstWord = words[0]; 
		String secondWord = words[1];
		System.out.println("secondWord:" + secondWord);
		if (secondWord != "" || secondWord!= null ) {
			String jsons = stringToJSON(firstWord, secondWord);
			client.getRoom().postMessage(jsons);
		}else {
			String jsons = stringToJSON("Room", "incomplete message");
			client.getRoom().postMessage(jsons);
		}
	}
	
	private String stringToJSON(String key, String value) {
		Message messageObj = new Message(key, value);
		Gson gsonObj = new Gson();
		String json = gsonObj.toJson(messageObj);
		return json;
	}
	
	private String read(int firstByte) throws IOException, BadRequestException {
		System.out.println("read function start ");
		//read first byte,
		int finOpcode = firstByte;
		//read in first payload byte
		int payloadLen = inputStream.read();
		//filter off the mask bit
		payloadLen = payloadLen & 0x0000007F;
		if(payloadLen == 126) {
			byte [] mediumpayloadLen = new byte [2];
			payloadLen = inputStream.read(mediumpayloadLen, 0, 2);
			int byte1 = mediumpayloadLen[0];
			int byte2 = mediumpayloadLen[1];
			
			int length1 = (byte1 << 8) & 0x0000FF00;
			int length2 = byte2 & 0x000000FF;
			
			int ret = length1 | length2;
			payloadLen = ret;
			
		}else if (payloadLen == 127) {
			throw new BadRequestException();
		}
		
		byte [] maskingKey = new byte [4];
		inputStream.read(maskingKey, 0, 4);
		byte [] encoded = new byte [payloadLen];
		byte [] decoded = new byte [payloadLen];
	
		
		inputStream.read(encoded, 0, payloadLen);
		decoded = decodeMessage(encoded, maskingKey);
		String message = new String(decoded);
		System.out.println(message);
		
		return message;
	}
	
	private byte [] decodeMessage(byte[] encoded, byte[] maskingKey) {
		byte [] DECODED = new byte [encoded.length];
		for (int i = 0; i < encoded.length; i++) {
		    DECODED[i] = (byte) (encoded[i] ^ maskingKey[i % 4]);
		}
		
		return DECODED;
	}
	/**********************
	 * 
	 * BUild Frame FUNCTION
	 * 
	 * 
	 * **********************/
	public void buildFrame(String type, String m) throws BadRequestException, IOException {
		System.out.println("build frame function start \r\n\r\n");
		 buildFinOpcode(type);
		 buildPayloadLen1(m);
		int payloadLen2 = 0;
		long payloadLen3 = (long) 0;
		
		if (m.getBytes().length > 126 && m.getBytes().length < max16Bits ) {
			payloadLen2 = build16BitPayloadLen(m);
			int extraPLByte1 = (payloadLen2 >> 8)& 0x000000FF;
			int extraPLByte2 = payloadLen2 & 0x000000FF;
			outputStream.write(extraPLByte1);
			outputStream.write(extraPLByte2);
		
		}else if(m.getBytes().length > max16Bits) {
			throw new BadRequestException();
		}
		outputStream.write(m.getBytes());
	
	}
	
	
	
	public int build16BitPayloadLen(String payload) {
		System.out.println("hello from 16BitPayload Builder");
		int mask = 0x0000FFFF; 
		int length = payload.getBytes().length;
		int ret = (mask & length);
		System.out.println("16bit length: " + Integer.toHexString(ret));
		return ret;
	}
	
	public Long build64BitPayloadLen(String payload) {
		Long ret = (long) 0x0; 
		int length = payload.getBytes().length;
		return (Long) (ret | length);
	}
	
	public void buildFinOpcode(String type) throws IOException {
		System.out.println("hello from buildFinOpcode");
		byte bit1 = (byte)0x80;
		byte ret = 0x00;
		
		if(type == "binary") {
			int bit2 = 0x02;
			ret = (byte)(bit1 | bit2);		
		}else if(type == "text") {
			int bit2 = 0x01;
			ret = (byte)(bit1 | bit2);
		}else {
			clientSocket.close();
		}
		
		System.out.println("finished finOpcode: " + Integer.toHexString(ret));	
		outputStream.write(ret);
	
	}
	
	public void buildPayloadLen1(String payload) throws BadRequestException, IOException {
		byte ret = 0x0000;
		int length = payload.getBytes().length;
		
		//ret = (byte) (ret | length);
		if(length < 126){
			System.out.println("payload is 125 or less");
			ret = (byte) (ret | length);
		}else if(length >125 && length < max16Bits) {
			System.out.println("payload is 16bits or less");
			ret = (byte) (126);
		}else if(length >max16Bits && length < max64Bits) {
			
			ret = (byte) (127);
		}else {
			throw new BadRequestException();
		}
		System.out.println("finshed payload1: "+ Integer.toHexString(ret));
		outputStream.write(ret);	
	}
	
}


