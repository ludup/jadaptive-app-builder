package com.jadaptive.plugins.email;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.activation.DataSource;


public abstract class EmailAttachment implements DataSource {

	private String filename;
	private String contentType;
	
	public EmailAttachment(String filename, String contentType) {
		super();
		this.filename = filename;
		this.contentType = contentType;
	}
	
	public String getFilename() {
		return filename;
	}
	
	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	

}
