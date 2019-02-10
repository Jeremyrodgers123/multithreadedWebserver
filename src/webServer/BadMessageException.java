package webServer;

public class BadMessageException extends Exception{
	BadMessageException(){
		super("Bad Message given");
	}
}
