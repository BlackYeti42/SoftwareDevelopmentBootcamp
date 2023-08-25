package com.bank.database;

public class FilePath {

	static String get(String directoryName, String filename) {

		StringBuilder filePath = new StringBuilder(System.getProperty("user.dir"));

		filePath.append(System.getProperty("file.separator")).append(directoryName);
		filePath.append(System.getProperty("file.separator")).append(filename);

		return filePath.toString();
	}
}
