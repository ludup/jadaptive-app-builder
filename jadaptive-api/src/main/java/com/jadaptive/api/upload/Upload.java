package com.jadaptive.api.upload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload2.core.FileItemHeaders;
import org.apache.commons.fileupload2.core.FileItemInput;


public class Upload {

	FileItemInput item;
	public Upload(FileItemInput item) {
		this.item = item;
	}
	
	public String getFilename() {
		return item.getName();
	}
	
	public String getFormVariable() {
		return item.getFieldName();
	}

	public InputStream openStream() throws IOException {
		return item.getInputStream();
	}
	
	public String getContentType() {
		return item.getContentType();
	}
	
	public FileItemHeaders getHeaders() {
		return item.getHeaders();
	}

	
}
