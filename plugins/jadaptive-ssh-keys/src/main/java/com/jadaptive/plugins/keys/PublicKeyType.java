package com.jadaptive.plugins.keys;

public enum PublicKeyType {

	ED25519("ssh-ed25519", 0),
	RSA_2048("ssh-rsa", 2048),
	RSA_3192("ssh-rsa", 3192),
	RSA_4096("ssh-rsa", 4096),
	ECDSA_256("ecdsa", 256),
	ECDSA_384("ecdsa", 384),
	ECDSA_521("ecdsa", 584);
	
	String algorithm;
	int bits;
	
	PublicKeyType(String algorithm, int bits) {
		this.algorithm = algorithm;
		this.bits = bits;
	}

	@Override
	public String toString() {
		return getAlgorithm();
	}
	
	public String getAlgorithm() {
		return algorithm;
	}

	public int getBits() {
		return bits;
	}
}
