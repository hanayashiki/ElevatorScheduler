package elevatorScheduler;

public class FloorRequest extends Request {
	public int from;
	public Direction direction;
	FloorRequest() {
		super();
		type = Type.FR;
	}
	FloorRequest(int from, Direction direction, double time) {
		super();
		this.from = from;
		this.direction = direction;
		this.time = time;
		type = Type.FR;
	}
	@Override
	public String toString() {
		return "[" + this.type + "," + this.from + "," + this.direction + "," + (int)(this.time) + "]";
	}
	@Override
	public int getTarget() {
		return from;
	}
}
