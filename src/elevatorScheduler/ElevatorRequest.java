package elevatorScheduler;

public class ElevatorRequest extends Request {
	int target;
	ElevatorRequest() {
		super();
		type = Type.ER;
	}
	ElevatorRequest(int target, double time) {
		super();
		this.target = target;
		this.time = time;
		this.type = Type.ER;
	}
	@Override
	public String toString() {
		return "[" + this.type + "," + this.target + "," + (int)(this.time) + "]";
	}
	@Override
	public int getTarget() {
		return target;
	}
}
