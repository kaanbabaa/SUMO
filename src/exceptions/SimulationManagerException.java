package exceptions;

public class SimulationManagerException extends Exception {
	
	public SimulationManagerException(String message) {
		super(message);
	}

	public SimulationManagerException(String message, Throwable cause) {
		super(message, cause);
	}
}
