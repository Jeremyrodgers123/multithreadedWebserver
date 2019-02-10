package webServer;

public class BadRequestException extends Exception{
	public BadRequestException() {
		super("400 BAD Request");
	}
}
