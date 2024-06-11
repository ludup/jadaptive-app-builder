package com.jadaptive.api.files;

import java.io.IOException;
import java.io.InputStream;

public interface FileAttachmentService {

	public void registerProvider(String uuid, String name);

	FileAttachment createAttachment(InputStream in, String filename, String contentType, String formVariable) throws IOException;

	InputStream getAttachmentContent(String uuid) throws IOException;

}
