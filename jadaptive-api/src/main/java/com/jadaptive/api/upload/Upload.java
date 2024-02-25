package com.jadaptive.api.upload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.FileItemHeaders;
import org.apache.tomcat.util.http.fileupload.FileItemStream;

public class Upload {

	FileItemStream item;
	public Upload(FileItemStream item) {
		this.item = item;
	}
	
	public String getFilename() {
		return item.getName();
	}
	
	public String getFormVariable() {
		return item.getFieldName();
	}

	public InputStream openStream() throws IOException {
		return item.openStream();
	}
	
	public String getContentType() {
		return item.getContentType();
	}
	
	public FileItemHeaders getHeaders() {
		return item.getHeaders();
	}

	
}
