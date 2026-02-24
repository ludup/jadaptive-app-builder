package com.jadaptive.api.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

public interface FileAttachmentService {

	public void registerProvider(String uuid, String name);

	FileAttachment createAttachment(InputStream in, String filename, String contentType, String formVariable, String template) throws IOException;

	InputStream getAttachmentContent(String uuid) throws IOException;

	public FileAttachment getAttachment(String uuid);

	public void markForRemoval(Collection<FileAttachment> attachments);

	public void markForRemoval(String encoded);

	InputStream getInputStream(String path) throws IOException;

	public void deleteAttachment(FileAttachment attachment) throws IOException;

	OutputStream getOutputStream(String path, String contentType) throws IOException;

}
