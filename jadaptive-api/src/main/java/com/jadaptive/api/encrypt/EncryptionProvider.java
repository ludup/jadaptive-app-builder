package com.jadaptive.api.encrypt;

public interface EncryptionProvider {
	
	int priority();

	void init() throws Exception;

	String encrypt(String string) throws Exception;

	String decrypt(String substring) throws Exception;

}
