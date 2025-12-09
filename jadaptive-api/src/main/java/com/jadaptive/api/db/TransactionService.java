package com.jadaptive.api.db;

import java.io.Closeable;

public interface TransactionService {
	
	@FunctionalInterface
	public interface UncheckedCloseable extends Closeable {
		@Override
		void close();
	}
	
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

	void executeOrDelayUntilCommitted(Runnable r);

}
