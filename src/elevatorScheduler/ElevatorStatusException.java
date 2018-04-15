package elevatorScheduler;

public class ElevatorStatusException extends RuntimeException {
	String message;
	public ElevatorStatusException(String message) {
		this.message = message;
	}
}
