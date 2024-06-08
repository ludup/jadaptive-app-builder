package com.jadaptive.api.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.template.FieldTemplate;

public interface FileAttachmentStorage extends ExtensionPoint {

	String getUuid();
	
	InputStream getAttachmentContent(String attachmentUUID) throws FileNotFoundException;
	
	long getMaximumSize();

	FileAttachment createAttachment(InputStream in, String filename, String contentType, String formVariable) throws IOException;
}
