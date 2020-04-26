package com.jadaptive.api.x509;

public class MismatchedCertificateException extends Exception {

	private static final long serialVersionUID = -3898335439807988224L;

	public MismatchedCertificateException() {
	}

	public MismatchedCertificateException(String message) {
		super(message);
	}

	public MismatchedCertificateException(Throwable cause) {
		super(cause);
	}

	public MismatchedCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

}
