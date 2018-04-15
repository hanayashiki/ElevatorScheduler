package elevatorScheduler;

import java.util.LinkedList;
import java.util.Scanner;

import elevatorScheduler.Elevator.Action;

import java.io.InputStream;
import java.util.Iterator;

public class ElevatorScheduler {
    Scanner scanner = null;
    Parser parser = new Parser();

    LinkedList<Request> waitingList = new LinkedList<>();
    LinkedList<Request> pickUpList = new LinkedList<>();
    Elevator elevator = new Elevator();

    enum Status {UP, DOWN, STILL, WFS}

    ;
    Status status = Status.WFS;

    public ElevatorScheduler(InputStream stream) {
        scanner = new Scanner(stream);
    }

    private String readLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        } else {
            return null;
        }
    }

    private Request getNextRequest() {
        // TODO: exception, for unfinished pick-up request
        try {
            return parser.parse(readLine());
        } catch (InputException inputException) {
            System.out.println("INVALID" + " [" + inputException.message + "]");
            return getNextRequest();
        }
    }

    private void getPickUpRequests() {
        // @ require: elevator.mainRequest is set
        double maxTime =
    }

    public void readRequests() {
        // TODO: exception: INVALID
        while (true) {
            Request newRequest = getNextRequest();
            System.out.println("request read: " + newRequest);
            if (newRequest == null) {
                // indicates RUN
                break;
            } else {
                waitingList.add(newRequest);
            }
        }
    }

    private void printRequestDealt(Request request, double time, Status status) {
        String deal = "(" + request.getTarget() + "," + status + "," + time + ")";
        System.out.println(request.toString() + "/" + deal);
    }

    public void simulate() {
        while (waitingList.size() != 0 || elevator.currentRequest != null || elevator.nextMainRequest != null) {

        }
    }
}
