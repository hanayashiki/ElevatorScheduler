package elevatorScheduler;

public class Request {
	public enum Type { FR, ER };
	public double time;
	public Type type;
	public String toString() {
		return "";
	}
	public int getTarget() {
		return 0;
	}
}
