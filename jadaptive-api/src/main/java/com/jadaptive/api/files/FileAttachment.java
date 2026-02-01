package com.jadaptive.api.files;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = FileAttachment.RESOURCE_KEY, defaultColumn = "filename")
@TableView(defaultColumns = {"filename", "contentType", "size", "hash" })
public class FileAttachment extends AbstractUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = -2041494617246214405L;

	public static final String RESOURCE_KEY = "fileAttachments";

	@ObjectField(type = FieldType.TEXT, nameField = true)
	private String filename;

	@ObjectField(type = FieldType.TEXT)
	private String downloadUrl;
	
	@ObjectField(type = FieldType.TEXT)
	private String contentType;
	
	@ObjectField(type = FieldType.LONG)
	private Long size;
	
	@ObjectField(type = FieldType.TEXT)
	private String hash;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = FileStorageProvider.RESOURCE_KEY)
	private FileStorageProvider provider;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	private String formVariable;
	
	@ObjectField(type = FieldType.TEXT)
	private String attachedTo;
	
	public String getName() {
		return getFilename();
	}
	
	public String getFileName() {
		return getName();
	}
	
	public String getShortCode() {
		return getUuid();
	}
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public FileStorageProvider getProvider() {
		return provider;
	}

	public void setProvider(FileStorageProvider provider) {
		this.provider = provider;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getFormVariable() {
		return formVariable;
	}

	public void setFormVariable(String formVariable) {
		this.formVariable = formVariable;
	}

	public String getAttachedTo() {
		return attachedTo;
	}

	public void setAttachedTo(String attachedTo) {
		this.attachedTo = attachedTo;
	}
}
