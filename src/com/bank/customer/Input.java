package com.bank.customer;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Input {

	private static Scanner input = new Scanner(System.in);
	
	static void clearInputBuffer() {
		
	    if (input.hasNextLine()) {
	        input.nextLine();
	    }
	}


	static String getUserInput(String message) {
		
		System.out.print(message);		
		while(input.hasNext("\n")) {
			input.nextLine();
		}
		String userInput = input.nextLine();
		return userInput;
	}
	
	static int getIntFromUser(String message, int lower, int upper) {
		
		System.out.print(message);
//		while(input.hasNext("\n")) {
//		    input.next();
//		}
		while(true) {
			try {				
				int in = input.nextInt();
				if (in < lower || in > upper) {
					throw new IllegalArgumentException();
				}
				return in;
			}
			catch(InputMismatchException | IllegalArgumentException e) {
				System.out.printf("Invalid input: you may only enter a whole number between %d and %d.\n", lower, upper);
				input.next();
			}
		}
	}

	static void closeScanner() {

		if (input != null)
			input.close();
	}
}