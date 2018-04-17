package elevatorScheduler;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String args[]) throws FileNotFoundException {
        ElevatorScheduler elevatorScheduler = new ElevatorScheduler(System.in);
        elevatorScheduler.readRequests();
        elevatorScheduler.simulate();
        elevatorScheduler.outputStandardOutputList();
    }
}
