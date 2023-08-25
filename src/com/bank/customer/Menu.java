package com.bank.customer;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bank.database.DatabaseConnect;

public class Menu {
	
	private static Account selectedAccount = null;
	
	static void mainMenu(User user, DatabaseConnect dbConnection) {

		while (true) {
			String message = getMenuMessage();
			BigDecimal value;
			
			int option = Input.getIntFromUser(message, 1, 7);
			
			try {
				switch(option) {
					case 1: //check balance
						System.out.printf("£%.2f\n\n", selectedAccount.getBalance());
						break;
						
					case 2: //make withdrawal
						value = getMoneyQuantity("Enter the ammount of money to withdraw:\n");
						selectedAccount.makeWithdrawal(value, dbConnection, false);
						break;
						
					case 3: //make deposit
						value = getMoneyQuantity("Enter the ammount of money to deposit:\n");
						selectedAccount.makeDeposit(value, dbConnection, false);
						break;
						
					case 4: //make transfer
						value = getMoneyQuantity("Enter the ammount of money to transfer:\n");
						String accountNumberPrompt = "Enter the account number of the account you wish to transfer to.\n";
						String accountNumber = getAccountNumber(accountNumberPrompt);

						if (!checkAccountNumber(accountNumber, dbConnection)) {
							System.out.print("Transfer failed.\n");
							break;
						}
						Account recipient = loadAccount(accountNumber, dbConnection);
						if(recipient != null) {
							selectedAccount.makeTransfer(value, recipient, dbConnection);
						}
						break;
						
					case 5: //create account
						createAccount(user, dbConnection);
						break;
						
					case 6: //switch account
						selectAccount(user, dbConnection);
						break;
					
					default:
						return;	//TODO fix bug where you sometimes have to input 7 multiple times to exit.	
				}
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		}
	}

	static void selectAccount(User user, DatabaseConnect dbConnection) {
		
		ArrayList<Account> accounts;
		String accountSelectionMessage;
		int selection;

		if(user.getTotalAccounts() == 3) {
			accountSelectionMessage = getAccountTrinarySelectionMessage();
			accounts = getAccounts(user);
			selection = Input.getIntFromUser(accountSelectionMessage, 1, 3);
			selectedAccount = accounts.get(selection - 1);
			System.out.print("Account selected\n");
			mainMenu(user, dbConnection);
			
			return;
		}
		
		if(user.getTotalAccounts() == 2) {
			accounts = getAccounts(user);
			accountSelectionMessage = getAccountBinarySelectionMessage(accounts);
			selection = Input.getIntFromUser(accountSelectionMessage, 1, 2);
			selectedAccount = accounts.get(selection - 1);
			System.out.print("Account selected\n");
			mainMenu(user, dbConnection);
			return;
		}
		
		if(user.getTotalAccounts() == 1) {
			accounts = getAccounts(user);
			selectedAccount = accounts.get(0);
			mainMenu(user, dbConnection);
			return;
		}
		
		String input = Input.getUserInput("You have no account. Would you like to create one? [y]/n\n");
		if(!input.equalsIgnoreCase("y")) {
			System.out.print("Exiting\n");
			return;
		}

		try {
			selectedAccount = createAccount(user, dbConnection);
			mainMenu(user, dbConnection);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	
	private static Account createAccount(User user, DatabaseConnect dbConnection) throws SQLException{
		
		String message = getAccountCreationMessage();
		int accountType = Input.getIntFromUser(message, 1, 3);
		switch(accountType) {
			case Account.SMALL_BUSINESS_ACCOUNT:
				user.createSmallBusinessAccount(dbConnection);
				return user.getSmallBusinessAccount();
			case Account.COMMUNITY_ACCOUNT:
				user.createCommunityAccount(dbConnection);
				return user.getCommunityAccount();
			default:
				user.createClientAccount(dbConnection);
				return user.getClientAccount();
		}
	}
	
	private static ArrayList<Account> getAccounts(User user){
		
		ArrayList<Account> accounts = new ArrayList<>(user.getTotalAccounts());
		
		SmallBusinessAccount smallBusinessAccount = user.getSmallBusinessAccount();
		if(smallBusinessAccount != null) {
			accounts.add(smallBusinessAccount);
		}
		
		CommunityAccount communityAccount = user.getCommunityAccount();
		if(communityAccount != null) {
			accounts.add(communityAccount);
		}
		
		ClientAccount clientAccount = user.getClientAccount();
		if(clientAccount != null) {
			accounts.add(clientAccount);
		}
		
		return accounts;
	}

	private static Account loadAccount(String accountNumber, DatabaseConnect dbConnection) throws SQLException {

		String query = "SELECT balance, account_type, restriction FROM accounts WHERE account_number = ?";

		ResultSet result = dbConnection.retrieve(query, accountNumber);
		BigDecimal balance;
		if (result.next()) {
			balance = result.getBigDecimal("balance");
		}
		else {
			System.err.print("Destination account not found. Could not make transfer.\n");
			return null;
		}
		int accountType = result.getInt("account_type");
		boolean restriction = result.getBoolean("restriction");

		switch (accountType) {
			case Account.SMALL_BUSINESS_ACCOUNT:
				return new SmallBusinessAccount(restriction, balance, accountNumber);

			case Account.COMMUNITY_ACCOUNT:
				return new CommunityAccount(restriction, balance, accountNumber);

			default:
				return new ClientAccount(restriction, balance, accountNumber);
		}
	}

	private static String getAccountNumber(String message) {
		
		while (true) {
//			Input.clearInputBuffer();
			String accountNumber = Input.getUserInput(message);
			Pattern pattern = Pattern.compile("^[0-9]{8}$");
			Matcher matcher = pattern.matcher(accountNumber);
			if (!matcher.find() || accountNumber.equals("00000000")) {
				System.out.print(
						"Invalid account number format. The number must consist of 8 digits which cannot all be 0.");
				continue;
			}			
			return accountNumber;
		}
	}
	
	private static boolean checkAccountNumber(String accountNumber, DatabaseConnect dbConnection) throws SQLException{
		
		String query = "SELECT 1 FROM accounts WHERE account_number = ?";

		ResultSet result = dbConnection.retrieve(query, accountNumber);
		if (result.next()) {
			return true;
		}
		System.out.print("No account with that number exists. Please double check the number.\n");
		return false;
	}
	
	private static BigDecimal getMoneyQuantity(String message) {
		
		while (true) {
			Input.clearInputBuffer();
			String value = Input.getUserInput(message);
			Pattern pattern = Pattern.compile("^([1-9][0-9]*|[0-9])\\.[0-9]{2}$");
			Matcher matcher = pattern.matcher(value);
			if (matcher.find()) {
				return new BigDecimal(value);
			}			
			System.out.print("Invalid number format. The number must be a decimal value ending in 2 decimal places.\n");
		}
	}
	
	private static String getAccountBinarySelectionMessage(ArrayList<Account> accounts) {
		
		StringBuilder message = new StringBuilder();
		message.append("Select an account:\n");
		message.append(String.format("1: %s\n", getAccountType(accounts, 0)));
		message.append(String.format("2: %s\n", getAccountType(accounts, 1)));
		return message.toString();
	}
	
	private static String getAccountType(ArrayList<Account> accounts, int index) {
		
		int accountType = accounts.get(index).getAccountType();
		switch(accountType) {
			case Account.SMALL_BUSINESS_ACCOUNT:
				return "Small Business Account";
			case Account.COMMUNITY_ACCOUNT:
				return "Community Account";
			default:
				return "Client Account";
		}
	}
	
	private static String getAccountTrinarySelectionMessage() {
		
		StringBuilder message = new StringBuilder();
		message.append("Select an account:\n");
		message.append("1: Small Business Account\n");
		message.append("2: Community Account\n");
		message.append("3: Client Account\n");
		return message.toString();
	}
	
	private static String getAccountCreationMessage() {
		
		StringBuilder message = new StringBuilder();
		message.append("Enter which type of account you would like to create:\n");
		message.append("1: Small Business Account\n");
		message.append("2: Community Account\n");
		message.append("3: Client Account\n");
		return message.toString();
	}
	
	private static String getMenuMessage() {
		
		StringBuilder message = new StringBuilder();
		message.append("Select which option you want:\n");
		message.append("1: Check balance\n");
		message.append("2: Make withdrawal\n");
		message.append("3: Make deposit\n");
		message.append("4: Make transfer\n");
		message.append("5: Create account\n");
		message.append("6: Switch account\n");
		message.append("7: Exit\n");
		return message.toString();
	}
}
