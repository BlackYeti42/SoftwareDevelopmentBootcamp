package com.bank.customer;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import com.bank.database.DatabaseConnect;

public abstract class Account {

	public static final int SMALL_BUSINESS_ACCOUNT = 1;
	public static final int COMMUNITY_ACCOUNT = 2;
	public static final int CLIENT_ACCOUNT = 3;

	private final String ACCOUNT_NUMBER;
	private BigDecimal balance;
	private final BigDecimal OVERDRAFT;
	private final boolean RESTRICTION;

	Account(boolean restriction, BigDecimal overdraft, BigDecimal balance, String accountNumber) {

		RESTRICTION = restriction;
		OVERDRAFT = overdraft;
		this.balance = balance;
		ACCOUNT_NUMBER = accountNumber;
	}

	BigDecimal getBalance() {

		return balance;
	}

	boolean getRestriction() {

		return RESTRICTION;
	}

	String getAccountNumber() {

		return ACCOUNT_NUMBER;
	}

	void makeDeposit(BigDecimal deposit, DatabaseConnect dbConnection, boolean asTransfer) {

		try {
			int id;
			int accountNumber = Integer.parseInt(ACCOUNT_NUMBER);
			BigDecimal newBalance = balance.add(deposit);
			String insertionQuery = "INSERT INTO deposit_history (account_id, deposit_time, ammount) VALUES (?, ?, ?)";
			String idQuery = buildIdQuery();

			updateBalance(dbConnection, newBalance);
			balance = newBalance;

			if (!asTransfer) {
				ResultSet result = dbConnection.retrieve(idQuery, accountNumber);
				if (result.next()) {
					id = result.getInt("id");
				}
				else {
					System.err.print("Could not get id.\n");
					balance = balance.subtract(deposit);
					System.err.print("Could not make deposit.\n");
					return;					
				}

				Instant instant = Instant.now();

				dbConnection.insertIntoHistoryTables(insertionQuery, id, instant, deposit);
				System.out.printf("You have deposited £%.2f.\n\n", deposit);
			}

		} catch (SQLException e) {

			System.err.print("Could not make deposit.\n");
			e.printStackTrace();
		}
	}

	boolean makeWithdrawal(BigDecimal withdrawal, DatabaseConnect dbConnection, boolean asTransfer) {

		if (!checkPermissions(withdrawal, "withdraw")) {
			return false;
		}
		try {
			int id;
			int accountNumber = Integer.parseInt(ACCOUNT_NUMBER);
			BigDecimal newBalance = balance.subtract(withdrawal);
			String insertionQuery = "INSERT INTO withdrawal_history (account_id, withdrawal_time, ammount) VALUES (?, ?, ?)";
			String idQuery = buildIdQuery();

			updateBalance(dbConnection, newBalance);
			balance = newBalance;

			if (!asTransfer) {
				ResultSet result = dbConnection.retrieve(idQuery, accountNumber);
				if (result.next()) {
					id = result.getInt("id");
					
				} else {
					System.err.print("Could not get id.\n");
					balance = balance.add(withdrawal);
					System.err.print("Could not make deposit.\n");
					return false;
				}

				Instant instant = Instant.now();

				dbConnection.insertIntoHistoryTables(insertionQuery, id, instant, withdrawal);
				System.out.printf("You have withdrawn £%.2f.\n\n", withdrawal);
			}
		} catch (SQLException e) {

			System.err.print("Could not make withdrawal.\n\n");
			e.printStackTrace();
			return false;
		}
		return true;		
	}

	void makeTransfer(BigDecimal transfer, Account recipient, DatabaseConnect dbConnection) {

		if (checkPermissions(transfer, "transfer")) {
			int sourceAccountId = 0;
			int destinationAccountId = 0;
			int sourceAccountNumber = Integer.parseInt(ACCOUNT_NUMBER);
			int destinationAccountNumber = Integer.parseInt(recipient.getAccountNumber());
			String insertionQuery = "INSERT INTO transfer_history (source_account_id, destination_account_id, transfer_time, ammount) VALUES (?, ?, ?, ?)";
			String idQuery = buildIdQuery();

			if(!makeWithdrawal(transfer, dbConnection, true)) {
				return;
			}
			recipient.makeDeposit(transfer, dbConnection, true);			

			try {
				ResultSet result = dbConnection.retrieve(idQuery, sourceAccountNumber);
				if (result.next()) {
					sourceAccountId = result.getInt("id");
				}
				else {
					System.err.print("Could not get source account id.\n");
				}

				result = dbConnection.retrieve(idQuery, destinationAccountNumber);
				if (result.next()) {
					destinationAccountId = result.getInt("id");
				}
				else {
					System.err.print("Could not get destination account id.\n");
				}

				Instant instant = Instant.now();

				if (sourceAccountId != 0 && destinationAccountId != 0) {
					dbConnection.insertIntoHistoryTables(insertionQuery, sourceAccountId, destinationAccountId, instant,
							transfer);
					System.out.printf("You have transferred £%.2f.\n\n", transfer);
				}
				else {
					System.out.print("Could not transfer funds.\n");
				}

			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
	}

	private String buildIdQuery() {

		StringBuilder query = new StringBuilder();
		query.append("SELECT id ");
		query.append("FROM accounts ");
		query.append("WHERE account_number = ?");
		return query.toString();
	}

	private void updateBalance(DatabaseConnect dbConnection, BigDecimal update) throws SQLException {

		String query = generateUpdateQuery();
		dbConnection.update(query, update, ACCOUNT_NUMBER);
	}

	private boolean checkPermissions(BigDecimal amount, String operation) {
		if (RESTRICTION) {
			System.out.printf("This account has multiple signatories, you cannot %s money at present.\n\n", operation);
			return false;
		}
		if (balance.add(OVERDRAFT).compareTo(amount) == -1) {
			System.out.printf("Insufficient balance to %s funds.\n\n", operation);
			return false;
		}
		return true;
	}

	private static String generateUpdateQuery() {

		StringBuilder update = new StringBuilder();

		update.append("UPDATE accounts ");
		update.append("SET balance = ? ");
		update.append("WHERE account_number = ?");

		return update.toString();
	}

	abstract int getAccountType();
}
