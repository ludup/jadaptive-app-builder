package com.jadaptive.api.db;

public interface TransactionService {
	
	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Exception;
	}

	@FunctionalInterface
	public interface TX {
		void undoable(ThrowingRunnable rollback);

	}
	
	void executeTransaction(Runnable r);

	boolean isTransactionActive();

	TX tx();

}
