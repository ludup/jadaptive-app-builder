package com.jadaptive.api.db;

@SuppressWarnings("serial")
public class ContendedLockException extends RuntimeException {

	public ContendedLockException(String taskName) {
		super(taskName);
	}
}
