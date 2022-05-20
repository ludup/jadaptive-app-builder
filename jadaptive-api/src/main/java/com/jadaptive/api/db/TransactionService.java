package com.jadaptive.api.db;

public interface TransactionService {

	void executeTransaction(Runnable r);

}
