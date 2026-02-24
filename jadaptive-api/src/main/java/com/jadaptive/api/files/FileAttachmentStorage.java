package com.jadaptive.api.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pf4j.ExtensionPoint;

public interface FileAttachmentStorage extends ExtensionPoint {

	String getUuid();
	
	InputStream getAttachmentContent(String attachmentUUID) throws FileNotFoundException, IOException;
	
	long getMaximumSize();

	FileAttachment createAttachment(InputStream in, String filename, String contentType, String formVariable, String template) throws IOException;

	String getName();

	InputStream getInputstream(String path) throws IOException;
	
	void deleteAttachment(FileAttachment object) throws IOException;

	OutputStream getOutputStream(String path, String contentType) throws IOException;
}
