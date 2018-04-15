package elevatorScheduler;

public class Elevator {
	public final static double SPEED = 2;
	public final static double DOOR_TIME = 1;
	public final static int TOP_FLOOR = 10;
    public final static int BOTTOM_FLOOR = 1;

	enum Action { UP, DOWN, DOOR, FINISHED };
	// UP: going up, or stay still, because of a request
	// DOWN: going down, because of a request
	// DOOR: opening and closing door
	// FINISHED: just finished a request, regardless of future unfinished request
	public Action action;
	public int position;
	public int target;
	public double simuTime = 0;
	
	public Request mainRequest = null;
	public Request currentRequest = null;
	public Request nextMainRequest = null;

	Elevator() {
		action = Action.FINISHED;
		position = 1;
	}
}
