package elevatorScheduler;

import java.text.NumberFormat;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser {
	private static final String floorRequest = "\\(FR,(\\+?[0-9]+),(UP|DOWN),(\\+?[0-9]+)\\)";
	private static final String elevatorRequest = "\\(ER,(\\+?[0-9]+),(\\+?[0-9]+)\\)";
	private static final Pattern floorRequestPattern = Pattern.compile(floorRequest);
	private static final Pattern elevatorRequestPattern = Pattern.compile(elevatorRequest);

	int index = 0;
	double lastTime = 0;
	
	public Request parse(String requestLine) throws InputException {
		if (requestLine == null) {
			return null;
		}
		String requestLineTrimmed = requestLine.replace(" ", "");
		Matcher floorRequestMatcher = floorRequestPattern.matcher(requestLineTrimmed);
		Matcher elevatorRequestMatcher = elevatorRequestPattern.matcher(requestLineTrimmed);
		Request newRequest = null;
		if (floorRequestMatcher.find()) {
			String directionString = floorRequestMatcher.group(2);
			int from;
			double time;
			try {
				from = Integer.parseInt(floorRequestMatcher.group(1));
				time = Integer.parseInt(floorRequestMatcher.group(3));
			} catch (NumberFormatException e) {
				throw new InputException(requestLine);
			}
			Direction direction = Direction.UP;
			if (directionString.equals("DOWN")) {
				direction = Direction.DOWN;
			}
			newRequest = new FloorRequest(from, direction, time);
		}
		else if (elevatorRequestMatcher.find()) {
			int target = Integer.parseInt(elevatorRequestMatcher.group(1));
			double time = Integer.parseInt(elevatorRequestMatcher.group(2));
			newRequest = new ElevatorRequest(target, time);
		}
		else if (requestLine.equals("RUN")) {
			return null;
		}
		else {
			throw new InputException(requestLine);
		}
		if (!checkSemantics(newRequest)) {
			throw new InputException(requestLine);
		}
		this.lastTime = newRequest.time;
		newRequest.order = index;
		index++;
		return newRequest;
	}

	public boolean checkSemantics(Request request) {
//		if (index == 0 && !(request.type == Request.Type.FR && request.time == 0.0)) {
//			return false;
//		}
		if (request.getTarget() > Elevator.TOP_FLOOR || request.getTarget() < Elevator.BOTTOM_FLOOR) {
			return false;
		}
		if (request.type == Request.Type.FR) {
			FloorRequest floorRequest = (FloorRequest) request;
			if (request.getTarget() == Elevator.BOTTOM_FLOOR && floorRequest.direction == Direction.DOWN) {
				return false;
			}
			if (request.getTarget() == Elevator.TOP_FLOOR && floorRequest.direction == Direction.UP) {
				return false;
			}
		}

		if (request.time < lastTime) {
			return false;
		}
		return true;
	}

	public static String format(double d) {
		NumberFormat NF = NumberFormat.getInstance();
		NF.setGroupingUsed(false);
		String str = NF.format(d);
		if (!str.contains(".")) {
			str += ".0";
		}
		return str;
	}
}
