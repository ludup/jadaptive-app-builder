package com.jadaptive.plugins.sshd;

import com.sshtools.common.publickey.SshKeyPairGenerator;

public enum KeyType {

	RSA("RSA"), DSA("DSA"), ECDSA("ECDSA"), ED25519("ED25519");

	private String keyType;

	private KeyType(String scheme) {
		if (scheme.equals("RSA")) {
			keyType = SshKeyPairGenerator.SSH2_RSA;
		} else if (scheme.equals("ECDSA")) {
			keyType = SshKeyPairGenerator.ECDSA;
		} else if (scheme.equals("ED25519")) {
			keyType = SshKeyPairGenerator.ED25519;
		}
	}

	@Override
	public String toString() {
		return keyType;
	}

	public static KeyType fromAlgorithm(String algorithm) {
		
		if(algorithm.equals("ssh-ed25519")) {
			return ED25519;
		} else if(algorithm.startsWith("ecdsa")) {
			return ECDSA;
		} else if(algorithm.equals("ssh-rsa")) {
			return RSA;
		} else if(algorithm.startsWith("rsa-sha2-")) {
			return RSA;
		}
		
		throw new IllegalArgumentException("Invalid algorithm " + algorithm);
	}
}
