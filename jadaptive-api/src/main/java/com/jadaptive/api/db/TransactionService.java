package com.jadaptive.api.db;

import java.io.Closeable;

import com.sshtools.gardensched.ThrowingRunnable;

public interface TransactionService {
	
	@FunctionalInterface
	public interface UncheckedCloseable extends Closeable {
		@Override
		void close();
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
