package tests;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;

import elevatorScheduler.*;

public class TestElevatorSchedulerByFile {
	public static void main(String args[]) throws FileNotFoundException {
		String sInputString =
				"(FR, 1, UP, 0)\n" +
				"(ER, 8, 1)";
		ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());

		FileInputStream testFile = null;
        NumberFormat NF = NumberFormat.getInstance();
		System.out.println(Parser.format((double)2147483647));
        System.out.println(Parser.format(2147483647.5));
		try {
			testFile = new FileInputStream("src/tests/testInputs/testUpDown.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ElevatorScheduler elevatorScheduler = new ElevatorScheduler(testFile);
		elevatorScheduler.readRequests();
		elevatorScheduler.simulate();
		System.out.println("------------------------");
		elevatorScheduler.outputStandardOutputList();
	}
}
