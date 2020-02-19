package com.jadaptive.api.encrypt;

public interface EncryptionService {

	boolean isEncrypted(String value);

	String encrypt(String value);

	String decrypt(String value);

}
