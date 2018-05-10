package elevatorScheduler;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.ArrayList;

import elevatorScheduler.Elevator.Action;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Comparator;
import java.text.NumberFormat;

public class ElevatorScheduler {
    Scanner scanner = null;
    Parser parser = new Parser();

    LinkedList<Request> waitingList = new LinkedList<>();

    Elevator elevator = new Elevator();

    enum Status {UP, DOWN, STILL, WFS}

    Status status = Status.WFS;

    LinkedList<String> standardOutputList = new LinkedList<>();

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

    private Request getPickUpRequest(Request nextRequest) {
        // @require: elevator.simuTime && elevator.position renewed
        double timeBound = elevator.predictReachingTime(nextRequest);
        Iterator<Request> iterator = waitingList.iterator();
        int index = 0;
        int minDistance;

        if (status == Status.UP) {
            minDistance = Integer.MAX_VALUE;
        } else {
            minDistance = Integer.MIN_VALUE;
        }
        int minIndex = 0;

        Request pickUp = null;

        while (iterator.hasNext()) {
            Request request = iterator.next();
            //System.out.println("checking for pick up: " + request);
            if (request.time >= timeBound) {
                //System.out.println(timeBound);
                break;
            }
            if (status == Status.UP && request.getTarget() <= elevator.position) {
                // System.out.println("denied 1: " + request);
                // System.out.println(request.getTarget() + ", " + elevator.position);
                index++;
                continue;
            }
            if (status == Status.DOWN && request.getTarget() >= elevator.position) {
                index++;
                continue;
            }
            int predictPosition;
            boolean accessible;
            if (status == Status.UP) { // when request arrives, will the elevator passed through it
                if (request.time > elevator.simuTime) {
                    predictPosition = (int) ((request.time - elevator.simuTime) * Elevator.SPEED) + elevator.position;
                    accessible = predictPosition < request.getTarget();
                } else {
                    accessible = elevator.position < request.getTarget();
                    // System.out.println(request.getTarget());
                }
                if (request.type == Request.Type.FR) {
                    accessible = accessible && request.getTarget() <= nextRequest.getTarget()
                            && ((FloorRequest) request).direction == Direction.UP;
                }
            } else {
                if (request.time > elevator.simuTime) {
                    predictPosition = - (int) ((request.time - elevator.simuTime) * Elevator.SPEED) + elevator.position;
                    accessible = predictPosition > request.getTarget();
                } else {
                    accessible = elevator.position > request.getTarget();
                    // System.out.println(request.getTarget());
                }
                if (request.type == Request.Type.FR) {
                    accessible = accessible && request.getTarget() >= nextRequest.getTarget()
                            && ((FloorRequest) request).direction == Direction.UP;
                }
            }
            if (accessible) {
                if ((status == Status.UP && request.getTarget() < minDistance) ||
                        (status == Status.DOWN && request.getTarget() > minDistance)) {
                    minIndex = index;
                    pickUp = request;
                    minDistance = request.getTarget();
                }
            }
            index++;
        }
        if (pickUp != null) {
//          System.out.println("Remoeved: " + waitingList.get(minIndex));
            waitingList.remove(minIndex);
//            System.out.println(pickUp);
//            System.out.println("pickUp waitingList: " + waitingList);
            //System.out.println(waitingList);
        }
        return pickUp;
    }

    public void readRequests() {
        // TODO: exception: INVALID
        while (true) {
            Request newRequest = getNextRequest();
            // System.out.println(newRequest);
            if (newRequest == null) {
                // indicates RUN
                break;
            } else {
                waitingList.add(newRequest);
            }
        }
        // System.out.println("-------------");
    }

    private void printRequestDealt(Request request, double time, Status status) {

        String deal = "(" + request.getTarget() + "," + status + "," + Parser.format(time) + ")";
        String out = request.toString() + "/" + deal;
        // System.out.println(out);
        standardOutputList.add(out);
    }

    private void printSameRequest(Request request) {
        String out = "#SAME " + request;
        // System.out.println(out);
        standardOutputList.add(out);
    }

    public void outputStandardOutputList() {
        for (String s : standardOutputList) {
            System.out.println(s);
        }
    }

//    private void checkWaitingListForLifting(Request request) {
//        Iterator<Request> iterator = waitingList.iterator();
//        // System.out.println(waitingList);
//        while (iterator.hasNext()) {
//            Request check = iterator.next();
//            if (check.getTarget() == request.getTarget() && check.time < elevator.simuTime) {
//                iterator.remove();
//                // System.out.println("lifting:");
//                printRequestDealt(check, elevator.simuTime, status);
//            }
//        }
//    }

    Comparator comparatorOnFloor = new Comparator<Request>() {
        @Override
        public int compare(Request request1, Request request2) {
            if (request1.getTarget() < request2.getTarget()) {
                if (status == Status.UP) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (status == Status.DOWN) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    };

    private boolean judgeSameRequestOnEffect(Request effect, Request test) {
        // @ require: simuTime before when effect just ends(door closed)
        if (effect == null) {
            return false;
        }
        if (effect.type == test.type &&
                effect.getTarget() == test.getTarget() &&
                elevator.simuTime >= test.time) {
            if (test.type == Request.Type.FR) {
                return ((FloorRequest)test).direction == ((FloorRequest)effect).direction;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private ArrayList<Request> pickUp() {
        ArrayList<Request> unfinishedList = new ArrayList<>();
        LinkedList<Request> pickUpQueue = new LinkedList<>();
        LinkedList<Request> groupFinished = new LinkedList<>();
        // System.out.println("Finding pick up for: " + elevator.mainRequest);
        // System.out.println("one pick");
        while (true) {
            boolean newAdded = false;
            boolean newDealt = false;
            // get possible pickups from this sight
            while (true) {
                Request request = getPickUpRequest(elevator.mainRequest);
                // System.out.println("Got pick up: " + request);
                if (request == null) {
                    break;
                } else {
                    newAdded = true;
                    pickUpQueue.add(request);
                }
            }
            // deal with a request, moving forward
            pickUpQueue.sort(comparatorOnFloor);
            // System.out.println("pickUpQueue: " + pickUpQueue);
            // get nearest request
            // System.out.println("pickUpQueue: " + pickUpQueue);
            if (pickUpQueue.size() > 0) {
                newDealt = true;
//                System.out.println("---pickUpQueue:" + pickUpQueue);
                Request pickUp = pickUpQueue.removeFirst();
                if ((status == Status.UP && pickUp.getTarget() < elevator.mainRequest.getTarget()) ||
                        (status == Status.DOWN && pickUp.getTarget() > elevator.mainRequest.getTarget())) {
                    elevator.simuTime += Math.abs(pickUp.getTarget() - elevator.position) / Elevator.SPEED;
                    elevator.position = pickUp.getTarget();
//                    System.out.print("pick up: ");
                    boolean same = false;
                    for (Request r: groupFinished) {
                        same = same | judgeSameRequestOnEffect(r, pickUp);
                    }
                    if (same) {
                        printSameRequest(pickUp);
                    } else {
                        printRequestDealt(pickUp, elevator.simuTime, status);
                        groupFinished.add(pickUp);
                        elevator.simuTime += Elevator.DOOR_TIME;
                    }
                } else {
                    unfinishedList.add(pickUp);
                    // System.out.println("cannot finish:" + pickUp);
                }
            }

            if (newAdded == false && newDealt == false) {
//                System.out.println("---unfinished list:" + unfinishedList);
                return unfinishedList;
            }
        }

    }

    public void simulate() {
        int pickUpFinished = 0;
        while (waitingList.size() != 0 || elevator.mainRequest != null || elevator.nextMainRequest != null) {
            if (status == Status.WFS) {
                // idle status
                Request newRequest;
                if (elevator.nextMainRequest == null) {
                    //System.out.println(elevator.mainRequest);
                    //System.out.println(elevator.nextMainRequest);
                    newRequest = waitingList.removeFirst();
                    // System.out.println("removed: " + newRequest);
                    while (true) {
                        boolean same = judgeSameRequestOnEffect(elevator.lastMainRequest, newRequest);
                        if (same) {
                            printSameRequest(newRequest);
                            if (waitingList.size() > 0) {
                                newRequest = waitingList.removeFirst();
                                // System.out.println("removed: " + newRequest);
                            } else {
                                return;
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    newRequest = elevator.nextMainRequest;
                    elevator.nextMainRequest = null;
                }
                elevator.simuTime = newRequest.time > elevator.simuTime ? newRequest.time : elevator.simuTime;
                elevator.mainRequest = newRequest;

                System.out.println("---mainRequest is: " + elevator.mainRequest);

                // pickUpList.clear();
                if (newRequest.getTarget() > elevator.position) {
                    status = Status.UP;
                } else if (newRequest.getTarget() < elevator.position) {
                    status = Status.DOWN;
                } else {
                    status = Status.STILL;
                }
                pickUpFinished = 0;
            } else {
                // do pick up
                ArrayList<Request> unfinishedList = pickUp();
                // do main request
                elevator.simuTime += Math.abs(elevator.mainRequest.getTarget() - elevator.position) / Elevator.SPEED;
                elevator.position = elevator.mainRequest.getTarget();

                // checkWaitingListForLifting(elevator.mainRequest);

                if (status == status.STILL) {
                    elevator.simuTime += Elevator.DOOR_TIME;
                    // System.out.println("main:");
                    printRequestDealt(elevator.mainRequest, elevator.simuTime, status);
                } else {
                    // System.out.println("main:");
                    printRequestDealt(elevator.mainRequest, elevator.simuTime, status);
                    elevator.simuTime += Elevator.DOOR_TIME;
                }

                if (unfinishedList.size() > 0) {

                    // Get the earliest unfinished pickup request
                    Request nextMainRequest = null;
                    unfinishedList.sort(comparatorOnFloor);
                    Iterator<Request> iterator = unfinishedList.iterator();
                    LinkedList<Request> groupFinished = new LinkedList<>();
                    boolean firstMain = false;
                    // System.out.println("unfinished: " + unfinishedList);
                    // deal with requests sharing target with main request
                    int groupingCount = 0;
                    while (iterator.hasNext()) {
                        Request request = iterator.next();
                        boolean same = false;
                        // judge same
                        for (Request r: groupFinished) {
                            same = same | judgeSameRequestOnEffect(r, request);
                        }
                        if (same) {
                            groupingCount++;
                            printSameRequest(request);
                            continue;
                        }
                        if (request.getTarget() == elevator.mainRequest.getTarget()) {
                            if (!judgeSameRequestOnEffect(elevator.mainRequest, request)) {
                                printRequestDealt(request, elevator.simuTime - Elevator.DOOR_TIME, status);
                                groupFinished.add(request);
                            } else {
                                printSameRequest(request);
                            }
                            groupingCount++;
                        }
                    }
                    // set new main request
                    for (int i = groupingCount; i < unfinishedList.size(); i++) {
                        if (nextMainRequest == null || unfinishedList.get(i).order < nextMainRequest.order) {
                            nextMainRequest = unfinishedList.get(i);
                        }
                    }
                    // restore unrun requests
                    for (int i = groupingCount; i < unfinishedList.size(); i++) {
                        if (unfinishedList.get(i) != nextMainRequest) {
                            waitingList.add(unfinishedList.get(i));
                            // System.out.println("Added: " + unfinishedList.get(i));
                        }
                    }

                    // System.out.println("after: " + waitingList);
                    // System.out.println(waitingList);
                    waitingList.sort(new Comparator<Request>() {
                        @Override
                        public int compare(Request request1, Request request2) {
                            if (request1.order < request2.order) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });
                    elevator.nextMainRequest = nextMainRequest;
                    elevator.lastMainRequest = elevator.mainRequest;
                    elevator.mainRequest = null;
                    status = Status.WFS;
                } else {
                    elevator.nextMainRequest = null;
                    elevator.lastMainRequest = elevator.mainRequest;
                    elevator.mainRequest = null;
                    status = Status.WFS;
                }
            }
        }
    }
}
