package com.jadaptive.plugins.keys;

public enum KeyType {

	RSA, DSA, ECDSA, ED25519;

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
