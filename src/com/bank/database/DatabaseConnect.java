package com.bank.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Scanner;

public class DatabaseConnect implements AutoCloseable {

	private final Connection CONNECTION;

	public DatabaseConnect() throws SQLException {

		File emailDetails = new File(FilePath.get("SetterFiles", "database_details.txt"));
		Scanner fileInput = null;
		try {
			fileInput = new Scanner(emailDetails);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		final String URL = fileInput.nextLine();
		final String USERNAME = fileInput.nextLine();
		final String PASSWORD = fileInput.nextLine();
		fileInput.close();

		CONNECTION = DriverManager.getConnection(URL, USERNAME, PASSWORD);
	}

	public ResultSet retrieve(String query) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		ResultSet result = statement.executeQuery();
		return result;
	}

	public ResultSet retrieve(String query, String columnName) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setString(1, columnName);
		ResultSet result = statement.executeQuery();
		return result;
	}

	public ResultSet retrieve(String query, int referenceValue) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, referenceValue);
		ResultSet result = statement.executeQuery();
		return result;

	}

	public void update(String query, byte[] binary, String columnName) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setBytes(1, binary);
		statement.setString(2, columnName);
		statement.executeUpdate();
	}

	public void update(String query, BigDecimal balance, String columnName) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setBigDecimal(1, balance);
		statement.setString(2, columnName);
		statement.executeUpdate();
	}

	public void update(String query, int newValue, int referenceValue) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, newValue);
		statement.setInt(2, referenceValue);
		statement.executeUpdate();
	}

	public void insertIntoHistoryTables(String query, int id, Instant instant, BigDecimal money) throws SQLException {

		java.sql.Timestamp timestamp = java.sql.Timestamp.from(instant);
		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, id);
		statement.setTimestamp(2, timestamp);
		statement.setBigDecimal(3, money);
		statement.executeUpdate();
	}

	public void insertIntoHistoryTables(String query, int sourceAccountId, int destinationAccountId, Instant instant,
			BigDecimal money) throws SQLException {

		java.sql.Timestamp timestamp = java.sql.Timestamp.from(instant);
		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, sourceAccountId);
		statement.setInt(2, destinationAccountId);
		statement.setTimestamp(3, timestamp);
		statement.setBigDecimal(4, money);
		statement.executeUpdate();
	}

	public void createNewAccount(String query, int accountNumber, BigDecimal balance, int accountType,
			boolean restriction) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, accountNumber);
		statement.setBigDecimal(2, balance);
		statement.setInt(3, accountType);
		statement.setBoolean(4, restriction);
		statement.executeUpdate();
	}

	public void createNewUser(String query, String username, String givenName,
			String familyName, String pinHash) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setString(1, username);
		statement.setString(2, givenName);
		statement.setString(3, familyName);
		statement.setString(4, pinHash);
		statement.executeUpdate();
	}

	public void insertNewAddresse(String query, int userId, String line1, String city, String county, String postcode,
			String countryCode, String email, String telephone1) throws SQLException {
		
		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, userId);
		statement.setString(2, line1);
		statement.setString(3, city);
		statement.setString(4, county);
		statement.setString(5, postcode);
		statement.setString(6, countryCode);
		statement.setString(7, email);
		statement.setString(8, telephone1);
		statement.executeUpdate();
	}

	public void createNewJoin(String query, int id) throws SQLException {

		PreparedStatement statement = CONNECTION.prepareStatement(query);
		statement.setInt(1, id);
		statement.executeUpdate();
	}

	@Override
	public void close() throws SQLException {

		if (CONNECTION != null && !CONNECTION.isClosed())
			CONNECTION.close();
	}
}