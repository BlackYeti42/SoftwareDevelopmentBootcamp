package com.bank.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class AuthenticatePin {

	private static final Argon2PasswordEncoder ENCODER;
	
	static {
		final int SALT_LENGTH = 32;
		final int HASH_LENGTH = 64;
		final int PARALLELISM = 1;
		final int MEMORY = 64 * 1024;
		final int ITERATIONS = 8;
		ENCODER = new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY, ITERATIONS);
	}

	public static void main(String[] args) {
		
		String username = "Detective";
		String pin = "1234";
		String givenName = "Sherlock";
		String familyName = "Holmes";
		String line1 = "221b Baker Street";
		String city = "London";
		String county = city;
		String postcode = "NW1 6XE";
		String countryCode = "GB";
		String email = "phil.restinghill@gmail.com";
		String telephone1 = "01234567890";
		String userCreationQuery = "INSERT INTO users (username, given_name, family_name, pin_hash) VALUES (?, ?, ?, ?)";
		String idQuery = "SELECT id FROM users WHERE username = ?";
		String joinTableQuery = "INSERT INTO users_accounts (user_id) VALUES (?)";
		String addressTableQuery = "INSERT INTO addresses (user_id, line_1, city, county, postcode, country_code, email, telephone_1) values (?, ?, ?, ?, ?, ?, ?, ?)";

		String hashCode = hashPin(pin);
		
		try (DatabaseConnect dbConnection = new DatabaseConnect();) {
			dbConnection.createNewUser(userCreationQuery, username, givenName, familyName, hashCode);
			ResultSet result = dbConnection.retrieve(idQuery, username);
			if (result.next()) {
				int id = result.getInt("id");
				dbConnection.createNewJoin(joinTableQuery, id);
				dbConnection.insertNewAddresse(addressTableQuery, id, line1, city, county, postcode, countryCode, email,
						telephone1);
			}
		}
		catch (SQLException e) {

			e.printStackTrace();
			System.exit(1);
		}		
	}

	public static boolean compareExpectedPinValue(String username, String pin, DatabaseConnect dbConnection) {

		String query = "SELECT pin_hash FROM users WHERE username = ?;";

		String hashCode = null;
		try {
			ResultSet result = dbConnection.retrieve(query, username);
			if (result.next()) {
				hashCode = result.getString("pin_hash");
				return ENCODER.matches(pin, hashCode);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	private static String hashPin(String pin) {

		String hashCode = ENCODER.encode(pin);
		return hashCode;	
		
	}
}
