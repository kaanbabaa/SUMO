package exceptions;

public class VehicleClassException extends Exception {

	public VehicleClassException(String message) {
		super(message);
	}
	
	public VehicleClassException(String message, Throwable cause) {
		super(message, cause);
	}
}
