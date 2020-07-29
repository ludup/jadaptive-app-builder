package com.jadaptive.api.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public interface EncryptionService {

	boolean isEncrypted(String value);

	String encrypt(String value);

	String decrypt(String value);

	String encrypt(String value, String keydata);

	String decrypt(String value, String keydata);

	boolean isEncryptedWithPassword(String value);

	String encryptString(String value, String base64Key, String base64Iv);
}
