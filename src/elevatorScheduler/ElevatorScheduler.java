package elevatorScheduler;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.ArrayList;

import elevatorScheduler.Elevator.Action;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Comparator;

public class ElevatorScheduler {
    Scanner scanner = null;
    Parser parser = new Parser();

    LinkedList<Request> waitingList = new LinkedList<>();

    Elevator elevator = new Elevator();

    enum Status {UP, DOWN, STILL, WFS}

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
                System.out.println("denied 1: " + request);
                System.out.println(request.getTarget() + ", " + elevator.position);
                continue;
            }
            if (status == Status.DOWN && request.getTarget() >= elevator.position) {
                continue;
            }
            int predictPosition;
            boolean accessible;
            if (status == Status.UP) {
                predictPosition = (int) ((request.time - elevator.simuTime) * Elevator.SPEED) + elevator.position;
                accessible = predictPosition < nextRequest.getTarget();
//                System.out.println(predictPosition);
//                System.out.println(nextRequest.getTarget());
//                System.out.println(accessible);
                if (request.type == Request.Type.FR) {
                    accessible = accessible && request.getTarget() <= nextRequest.getTarget();
//                    System.out.println(request.getTarget() + ", " + nextRequest.getTarget());
//                    System.out.println(accessible);
                }
            } else {
                predictPosition = -(int) ((request.time - elevator.simuTime) * Elevator.SPEED) + elevator.position;
                accessible = predictPosition > nextRequest.getTarget();
                if (request.type == Request.Type.FR) {
                    accessible = accessible && request.getTarget() >= nextRequest.getTarget();
                }
            }
            if (accessible) {
                if (status == Status.UP && request.getTarget() < minDistance ||
                        status == Status.DOWN && request.getTarget() > minDistance) {
                    minIndex = index;
                    pickUp = request;
                    minDistance = request.getTarget();
                }
            }
            index++;
        }
        if (pickUp != null) {
            waitingList.remove(minIndex);
            //System.out.println(waitingList);
        }
        return pickUp;
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

    private void pickUp() {
        ArrayList<Request> pickUpList = new ArrayList<>();
        LinkedList<Request> pickUpQueue = new LinkedList<>();
        while (true) {
            boolean newAdded = false;
            boolean newDealt = false;
            // get possible pickups from this sight
            while (true) {
                Request request = getPickUpRequest(elevator.mainRequest);
                if (request == null) {
                    break;
                } else {
                    newAdded = true;
                    pickUpQueue.add(request);
                }
            }
            // deal with a request, moving forward
            System.out.println("pickUpQueue: " + pickUpQueue);
            Comparator comparator = new Comparator<Request>() {
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
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
            };
            pickUpQueue.sort(comparator);
            // get nearest request
            if (pickUpQueue.size() > 0) {
                Request pickUp = pickUpQueue.removeFirst();
                if ((status == Status.UP && pickUp.getTarget() < elevator.mainRequest.getTarget()) ||
                        (status == Status.DOWN && pickUp.getTarget() > elevator.mainRequest.getTarget())) {
                    newDealt = true;
                    elevator.simuTime += Math.abs(pickUp.getTarget() - elevator.position) / Elevator.SPEED;
                    elevator.position = pickUp.getTarget();
                    System.out.println("pick up: ");
                    printRequestDealt(pickUp, elevator.simuTime, status);
                    elevator.simuTime += Elevator.DOOR_TIME;
                    //
                }
            }
            System.out.println(newAdded);
            System.out.println(newDealt);
            if (newAdded == false && newDealt == false) {
                break;
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
                    newRequest = waitingList.removeFirst();
                    elevator.simuTime = newRequest.time;
                } else {
                    newRequest = elevator.nextMainRequest;
                    elevator.nextMainRequest = null;
                }
                elevator.mainRequest = newRequest;

                System.out.println("mainRequest is: " + elevator.mainRequest);

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
                pickUp();
                // if null, no more can be added to pickUpList
//                elevator.simuTime += Math.abs(elevator.mainRequest.getTarget() - elevator.position) / Elevator.SPEED;
//                elevator.position = elevator.mainRequest.getTarget();
//                System.out.println("main: ");
//                if (status == Status.STILL) {
//                    printRequestDealt(elevator.mainRequest, elevator.simuTime + Elevator.DOOR_TIME, status);
//                } else {
//                    printRequestDealt(elevator.mainRequest, elevator.simuTime, status);
//                }
//                elevator.simuTime += Elevator.DOOR_TIME;
//                elevator.mainRequest = null;
//                status = Status.WFS;
//                if (pickUpFinished < pickUpList.size()) {
//                    System.out.println("pick up list: " + pickUpList);
//                    System.out.println("not finished pick up");
//                    elevator.nextMainRequest = pickUpList.get(pickUpFinished);
//                    for (int i = pickUpFinished + 1; i < pickUpList.size(); i++) {
//                        if (pickUpList.get(i).order < elevator.nextMainRequest.order) {
//                            elevator.nextMainRequest = pickUpList.get(i);
//                        }
//                    }
//                    System.out.println(elevator.nextMainRequest);
//                    // return deleted pick ups
//                    for (int i = pickUpFinished; i < pickUpList.size(); i++) {
//                        if (pickUpList.get(i).order != elevator.nextMainRequest.order) {
//                            waitingList.add(pickUpList.get(i));
//                        }
//                    }
//                    // restore order
//                    Comparator comparator = new Comparator<Request>() {
//                        @Override
//                        public int compare(Request request1, Request request2) {
//                            if (request1.order < request2.order) {
//                                return -1;
//                            } else {
//                                return 1;
//                            }
//                        }
//                    };
//                    waitingList.sort(comparator);
//                    System.out.println(waitingList);
//            } else{
//                elevator.nextMainRequest = null;
//            }
        }
    }
}
}
