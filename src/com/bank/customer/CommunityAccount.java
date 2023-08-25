package com.bank.customer;

import java.math.BigDecimal;

public class CommunityAccount extends Account {
	
	public CommunityAccount(boolean restriction, BigDecimal balance, String accountNumber) {
		
		super(restriction, new BigDecimal(2500), balance, accountNumber);
	}

	@Override
	int getAccountType() {
		
		return COMMUNITY_ACCOUNT;
	}
}
