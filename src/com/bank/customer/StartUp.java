package com.bank.customer;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bank.database.AuthenticatePin;
import com.bank.database.AuthenticateUsername;
import com.bank.database.AuthenticationEmail;
import com.bank.database.DatabaseConnect;
import com.bank.database.TemporaryPassword;

public class StartUp {

	public static void main(String[] args) {

		try (DatabaseConnect dbConnection = new DatabaseConnect()) {
			while (true) {
				
				String username = Input.getUserInput("Please enter your username to begin.\n");
				if (username.equalsIgnoreCase("exit"))
					break;
				login(username, dbConnection);	
				Input.clearInputBuffer();				
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		Input.closeScanner();
	}

	private static void login(String username, DatabaseConnect dbConnection) {

		if (!AuthenticateUsername.checkUsernameExists(username, dbConnection)) {
			System.out.print("No such username is recognised. Please check and try again.\n");
			return;
		}

		boolean authenticate = tryToAuthenticateThrice(username, dbConnection);
		if (authenticate) {
			System.out.print("You have exceeded the maximum number of failed attempts.\n");
			return;
		}

		System.out.print("Login succesful!\n");
		User user = loadUser(username, dbConnection);
		Menu.selectAccount(user, dbConnection);
		
		// TODO: must add an administrator check for system exit.
	}

	private static User loadUser(String username, DatabaseConnect dbConnection) {

		User user = new User(username);
		String query = buildAccountQuery();
		
		try {
			ResultSet accounts = dbConnection.retrieve(query, username);
			
			while (accounts.next()) {
				String accountNumber = accounts.getString("account_number");
				BigDecimal balance = accounts.getBigDecimal("balance");
				int accountType = accounts.getInt("account_type");
				boolean restriction = accounts.getBoolean("restriction");

				switch (accountType) {
					
					case Account.SMALL_BUSINESS_ACCOUNT:
						SmallBusinessAccount smallBusinessAccount = new SmallBusinessAccount(restriction, balance,
								accountNumber);
						user.setSmallBusinessAccount(smallBusinessAccount);
						break;
						
					case Account.COMMUNITY_ACCOUNT:
						CommunityAccount communityAccount = new CommunityAccount(restriction, balance, accountNumber);
						user.setCommunityAccount(communityAccount);
						break;
						
					default:
						ClientAccount clientAccount = new ClientAccount(restriction, balance, accountNumber);
						user.setClientAccount(clientAccount);
				}
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		
		return user;
	}
	
	private static String buildAccountQuery() {

		StringBuilder query = new StringBuilder();

		query.append("SELECT accounts.* ");
		query.append("FROM users ");
		query.append("JOIN users_accounts ON users_accounts.user_id = users.id ");
		query.append("JOIN accounts ON accounts.id = users_accounts.account_id ");
		query.append("WHERE users.username = ?");

		return query.toString();
	}

	private static boolean tryToAuthenticateThrice(String username, DatabaseConnect dbConnection) {

		boolean authenticated = false;
		for (int i = 0; i < 3; i++) {

			boolean testPinResult = testPin(username, dbConnection);			
			boolean testTempPasswordResult = testTempPassword(username, dbConnection);
			
			authenticated = testPinResult && testTempPasswordResult;
			if (authenticated)
				break;
			System.out.print("Authentication failure.\n");
		}
		return authenticated;
	}

	private static boolean testTempPassword(String username, DatabaseConnect dbConnection) {

		TemporaryPassword tempPassword = new TemporaryPassword();
		AuthenticationEmail.sendAuthenticationEmail(username, tempPassword, dbConnection);

		String password = Input.getUserInput("Enter your temporary password: \n");
		return password.equals(tempPassword.getPassword());
	}

	private static boolean testPin(String username, DatabaseConnect dbConnection) {

		String pin = Input.getUserInput("Please enter your pin.\n");
		return AuthenticatePin.compareExpectedPinValue(username, pin, dbConnection);
	}
}
