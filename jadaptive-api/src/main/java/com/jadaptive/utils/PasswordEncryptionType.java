package com.jadaptive.utils;

public enum PasswordEncryptionType {

	PBKDF2_SHA256_20000("PBKDF2WithHmacSHA256", 256, 20000),
	PBKDF2_SHA256_50000("PBKDF2WithHmacSHA256", 256, 50000),
	PBKDF2_SHA512_50000("PBKDF2WithHmacSHA512", 512, 50000),
	PBKDF2_SHA512_100000("PBKDF2WithHmacSHA512", 512, 100000);
	
	private final String val;
	private final int keyLength;
	private final int iterations;
	
	private PasswordEncryptionType(final String val, int keyLength, int iterations) {
		this.val = val;
		this.keyLength = keyLength;
		this.iterations = iterations;
	}
	
	public String toString() {
		return val;
	}
	
	public int getKeyLength() {
		return keyLength;
	}
	
	public int getIterations() {
		return iterations;
	}
}
