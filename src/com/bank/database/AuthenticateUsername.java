package com.bank.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticateUsername {

	public static boolean checkUsernameExists(String username, DatabaseConnect dbConnection) {

		String query = "SELECT 1 FROM users WHERE username = ?";
		try {
			ResultSet result = dbConnection.retrieve(query, username);
			if (result.next())
				return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return false;
	}
}
