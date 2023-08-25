package com.bank.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {

	static String readFile(String filePath) {

		StringBuilder fileToString = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				fileToString.append(line).append(System.lineSeparator());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileToString.toString();
	}
}
