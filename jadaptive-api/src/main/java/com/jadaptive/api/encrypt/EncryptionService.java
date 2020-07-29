package com.jadaptive.api.encrypt;

public interface EncryptionService {

	boolean isEncrypted(String value);

	String encrypt(String value);

	String decrypt(String value);

	String encrypt(String value, String keydata);

	String decrypt(String value, String keydata);

	boolean isEncryptedWithPassword(String value);

	String encryptString(String value, String base64Key, String base64Iv);
}
