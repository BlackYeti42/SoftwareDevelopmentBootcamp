package com.bank.customer;

import java.math.BigDecimal;

public class SmallBusinessAccount extends Account {
	
	public SmallBusinessAccount(boolean restriction, BigDecimal balance, String accountNumber) {
		
		super(restriction, new BigDecimal(1000), balance, accountNumber);
	}
	
	@Override
	int getAccountType() {
		
		return SMALL_BUSINESS_ACCOUNT;
	}
}
