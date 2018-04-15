package tests;
import elevatorScheduler.*;

public class TestParser {
	public static void main(String args[]) {
		System.out.println(new Parser().parse("(FR, 3, UP, 1)"));
		System.out.println(new Parser().parse("(FR, 5, DOWN, 1000)"));
		System.out.println(new Parser().parse("(ER, 5, 1000)"));
		System.out.println(new Parser().parse("(ER, 5, 0)"));
	}
}
