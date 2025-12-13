package exceptions;

public class RouteClassException extends Exception{

	public RouteClassException(String message) {
		super(message);
	}
	
	public RouteClassException(String message, Throwable cause) {
		super(message, cause);
	}
}
