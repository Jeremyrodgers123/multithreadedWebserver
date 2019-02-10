package webServer;

public class Message extends Object{
	String userName = "";
	String message = "";
	String type = "";
	Message(String _userName, String _message){
		userName = _userName;
		message = _message;
	}
}
