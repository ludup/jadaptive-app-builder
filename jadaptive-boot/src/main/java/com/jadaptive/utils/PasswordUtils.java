package com.jadaptive.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

	public static boolean authenticate(char[] attemptedPassword,
			byte[] encryptedPassword, byte[] salt, PasswordEncryptionType type)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		byte[] encryptedAttemptedPassword = getEncryptedPassword(
				attemptedPassword, salt, type);

		return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
	}

	public static byte[] getEncryptedPassword(char[] password, byte[] salt,
			PasswordEncryptionType type) throws NoSuchAlgorithmException,
			InvalidKeySpecException {

		KeySpec spec = new PBEKeySpec(password, salt, type.getIterations(),
				type.getKeyLength());

		SecretKeyFactory f = SecretKeyFactory.getInstance(type.toString());

		return f.generateSecret(spec).getEncoded();
	}

	public static byte[] generateSalt() throws NoSuchAlgorithmException {

		SecureRandom random = SecureRandom.getInstance("SHA256PRNG");

		byte[] salt = new byte[32];
		random.nextBytes(salt);

		return salt;
	}
}
