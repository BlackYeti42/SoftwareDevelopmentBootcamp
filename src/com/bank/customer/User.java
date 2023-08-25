package com.bank.customer;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bank.database.DatabaseConnect;

public class User {

	private static final int ACCOUNT_NUMBER_LENGTH = 8;

	private final String USERNAME;

	private SmallBusinessAccount smallBusinessAccount = null;
	private CommunityAccount communityAccount = null;
	private ClientAccount clientAccount = null;
	private int totalAccounts = 0;

	User(String username) {

		USERNAME = username;
	}

	void createSmallBusinessAccount(DatabaseConnect dbConnection) throws SQLException {

		if (smallBusinessAccount == null) {
			String accountNumber = getAccountNumber(dbConnection);
			int numericAccountNumber = Integer.parseInt(accountNumber);
			smallBusinessAccount = new SmallBusinessAccount(false, new BigDecimal(0), accountNumber);

			String accountCreationQuery = buildAccountCreationQuery();
			dbConnection.createNewAccount(accountCreationQuery, numericAccountNumber, smallBusinessAccount.getBalance(),
					smallBusinessAccount.getAccountType(), smallBusinessAccount.getRestriction());

			boolean test = updateJoinTable(numericAccountNumber, dbConnection);

			if (test) {
				System.out.print("Small Business Account created successfully.\n");
				totalAccounts++;
			}
			//TODO delete new table if test failed.
		} 
		else {
			System.out.print("You already have a Small Business Account, you cannot create another.\n");
		}
	}

	void createCommunityAccount(DatabaseConnect dbConnection) throws SQLException {

		if (communityAccount == null) {
			String accountNumber = getAccountNumber(dbConnection);
			int numericAccountNumber = Integer.parseInt(accountNumber);
			communityAccount = new CommunityAccount(false, new BigDecimal(0), accountNumber);

			String accountCreationQuery = buildAccountCreationQuery();

			dbConnection.createNewAccount(accountCreationQuery, numericAccountNumber, communityAccount.getBalance(),
					communityAccount.getAccountType(), communityAccount.getRestriction());

			boolean test = updateJoinTable(numericAccountNumber, dbConnection);

			if (test) {
				System.out.print("Community Account created successfully.\n");
				totalAccounts++;
			}
			//TODO delete new table if test failed.
		} 
		else {
			System.out.print("You already have a Community Account, you cannot create another.\n");
		}
	}

	void createClientAccount(DatabaseConnect dbConnection) throws SQLException {

		if (clientAccount == null) {
			String accountNumber = getAccountNumber(dbConnection);
			int numericAccountNumber = Integer.parseInt(accountNumber);
			clientAccount = new ClientAccount(false, new BigDecimal(0), accountNumber);

			String accountCreationQuery = buildAccountCreationQuery();
			
			dbConnection.createNewAccount(accountCreationQuery, numericAccountNumber, clientAccount.getBalance(),
					clientAccount.getAccountType(), clientAccount.getRestriction());
			
			boolean test = updateJoinTable(numericAccountNumber, dbConnection);

			if (test) {
				System.out.print("Client Account created successfully.\n");
				totalAccounts++;
			}
			//TODO delete new table if test failed.
		} 
		else {
			
			System.out.print("You already have a Client Account, you cannot create another.\n");
		}
	}
	
	private boolean updateJoinTable(int accountNumber, DatabaseConnect dbConnection) throws SQLException{
		
		String accountIdQuery = buildAccountIdQuery();
		String userIdQuery = buildUserIdQuery();
		String joinQuery = (totalAccounts == 0)? buildUpdateJoinQuery() : buildInsertIntoJoinQuery();
		
		ResultSet result = dbConnection.retrieve(accountIdQuery, accountNumber);
		int accountId;
		if (result.next()) {
			accountId = result.getInt("id");
		}
		else {
			System.err.print("Could not get accountId. Acount creation failed.");
			return false;
		}

		result = dbConnection.retrieve(userIdQuery, USERNAME);		
		int userId;
		if (result.next()) {
			userId = result.getInt("id");
		}
		else {
			System.err.print("Could not get userId. Acount creation failed.");
			return false;
		}

		dbConnection.update(joinQuery, accountId, userId);
		return true;
	}

	private static String buildUpdateJoinQuery() {

		StringBuilder query = new StringBuilder();
		query.append("UPDATE users_accounts ");
		query.append("SET account_id = ? ");
		query.append("WHERE user_id = ?");
		return query.toString();
	}
	
	private static String buildInsertIntoJoinQuery() {

		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO users_accounts (account_id, user_id) ");
		query.append("VALUES (?, ?)");
		return query.toString();
	}

	private static String buildUserIdQuery() {

		StringBuilder query = new StringBuilder();
		query.append("SELECT id ");
		query.append("FROM users ");
		query.append("WHERE username = ?");
		return query.toString();
	}

	private static String buildAccountIdQuery() {

		StringBuilder query = new StringBuilder();
		query.append("SELECT id ");
		query.append("FROM accounts ");
		query.append("WHERE account_number = ?");
		return query.toString();
	}

	private static String buildAccountCreationQuery() {

		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO accounts (account_number, balance, account_type, restriction) ");
		query.append("VALUES (?, ?, ?, ?)");
		return query.toString();
	}

	private String getAccountNumber(DatabaseConnect dbConnection) {

		String accountNumber = "00000001";

		try {
			String query = "SELECT MAX(account_number) AS max_account_number FROM accounts";
			ResultSet result = dbConnection.retrieve(query);

			if (result.next()) {
				accountNumber = String.format("%0" + ACCOUNT_NUMBER_LENGTH + "d",
						result.getInt("max_account_number") + 1);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return accountNumber;
	}

	void setSmallBusinessAccount(SmallBusinessAccount smallBusinessAccount) {

		this.smallBusinessAccount = smallBusinessAccount;
		totalAccounts++;
	}

	void setCommunityAccount(CommunityAccount communityAccount) {

		this.communityAccount = communityAccount;
		totalAccounts++;
	}

	void setClientAccount(ClientAccount clientAccount) {

		this.clientAccount = clientAccount;
		totalAccounts++;
	}

	SmallBusinessAccount getSmallBusinessAccount() {

		return smallBusinessAccount;
	}

	CommunityAccount getCommunityAccount() {

		return communityAccount;
	}

	ClientAccount getClientAccount() {

		return clientAccount;
	}

	public int getTotalAccounts() {

		return totalAccounts;
	}

	public String getUsername() {
		
		return USERNAME;
	}
}