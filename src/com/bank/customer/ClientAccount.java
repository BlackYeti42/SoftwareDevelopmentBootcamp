package com.bank.customer;

import java.math.BigDecimal;

public class ClientAccount extends Account {
	
	public ClientAccount(boolean restriction, BigDecimal balance, String accountNumber) {
		
		super(restriction, new BigDecimal(1500), balance, accountNumber);
	}
	
	@Override
	int getAccountType() {
		
		return CLIENT_ACCOUNT;
	}
}
