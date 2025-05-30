package com.jadaptive.api.db;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;

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
	
	UncheckedCloseable withLock(String lockName) throws ContendedLockException;
	
	default void runWithLock(String lockName, Runnable r)  throws ContendedLockException {
		try(@SuppressWarnings("unused")
		var lck = withLock(lockName)) {
			r.run();
		}
	}
	
	default <T> T callWithLock(String lockName, Callable<T> task) throws Exception {
		try(@SuppressWarnings("unused")
		var lck = withLock(lockName)) {
			return task.call();
		}
	}
	
	default <T> T callWithLockUnchecked(String lockName, Callable<T> task) {
		try {
			return callWithLock(lockName, task);
		}
		catch(IOException re) {
			throw new UncheckedIOException(re);
		}
		catch(RuntimeException re) {
			throw re;
		}
		catch(Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
