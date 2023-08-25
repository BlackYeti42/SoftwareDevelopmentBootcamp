package com.bank.database;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

public class TemporaryPassword {

	// An 8 character alphanumeric string containing at least 1 upper case letter, 1
	// lower case letter, and 1 number.
	private final String TEMPORARY_PASSWORD;

	private static SecureRandom secureRand = new SecureRandom();

	public TemporaryPassword() {

		final int STRING_LENGTH = 8;

		StringBuilder passwordBuilder = new StringBuilder(STRING_LENGTH);
		ArrayList<Character> passwordChars = new ArrayList<>(STRING_LENGTH);

		int i = 0;
		for (; i < 3; i++) {
			passwordChars.add(selectCharacterType(i));
		}
		for (; i < STRING_LENGTH; i++) {
			passwordChars.add(selectCharacterType(secureRand.nextInt(3)));
		}

		Collections.shuffle(passwordChars);
		for (Character ch : passwordChars) {
			passwordBuilder.append(ch);
		}
		TEMPORARY_PASSWORD = passwordBuilder.toString();
	}

	private static char selectCharacterType(int selection) throws IllegalArgumentException {

		final int CAPITAL_LOWER_BOUND = (int) 'A';
		final int CAPITAL_UPPER_BOUND = (int) 'Z';
		final int MINUSCULE_LOWER_BOUND = (int) 'a';
		final int MINUSCULE_UPPER_BOUND = (int) 'z';
		final int NUMERAL_LOWER_BOUND = (int) '0';
		final int NUMERAL_UPPER_BOUND = (int) '9';

		switch (selection) {
			case 0:
				return getPasswordCharacter(CAPITAL_LOWER_BOUND, CAPITAL_UPPER_BOUND);
			case 1:
				return getPasswordCharacter(MINUSCULE_LOWER_BOUND, MINUSCULE_UPPER_BOUND);
			case 2:
				return getPasswordCharacter(NUMERAL_LOWER_BOUND, NUMERAL_UPPER_BOUND);
			default:
				throw new IllegalArgumentException();
		}
	}

	private static char getPasswordCharacter(int lowerBound, int upperBound) {

		int randomInt = getRandomIntInRange(lowerBound, upperBound);
		return (char) randomInt;
	}

	private static int getRandomIntInRange(int lowerBound, int upperBound) {

		return secureRand.nextInt(upperBound - lowerBound + 1) + lowerBound;
	}

	public String getPassword() {

		return TEMPORARY_PASSWORD;
	}
}
