package com.jadaptive.api.x509;

public class FileFormatException extends Exception {

	private static final long serialVersionUID = -4583403433721017708L;

	public FileFormatException() {
	}

	public FileFormatException(String message) {
		super(message);
	}

	public FileFormatException(Throwable cause) {
		super(cause);
	}

	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
