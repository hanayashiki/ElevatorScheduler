package elevatorScheduler;

public class InputException extends RuntimeException {
	String message;
	InputException(String message) {
		this.message = message;
	}
}
