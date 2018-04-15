package tests;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import elevatorScheduler.*;

public class TestElevatorScheduler {
	public static void main(String args[]) throws FileNotFoundException {
		String sInputString = 
				"(FR, 1, UP, 0)\n" +
				"(ER, 8, 1)";
		ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes()); 
		
		FileInputStream testFile = null;
		try {
			testFile = new FileInputStream("src/tests/testInputs/testOfficialPickUp.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ElevatorScheduler elevatorScheduler = new ElevatorScheduler(testFile);
		elevatorScheduler.readRequests();
		elevatorScheduler.simulate();
	}
}
