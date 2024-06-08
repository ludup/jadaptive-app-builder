package com.jadaptive.api.files;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = FileAttachment.RESOURCE_KEY)
@TableView(defaultColumns = {"name", "contentType", "size", "hash", "provider.name" })
public class FileAttachment extends AbstractUUIDEntity {

	private static final long serialVersionUID = -2041494617246214405L;

	public static final String RESOURCE_KEY = "fileAttachments";

	@ObjectField(type = FieldType.TEXT)
	String filename;

	@ObjectField(type = FieldType.TEXT)
	String downloadUrl;
	
	@ObjectField(type = FieldType.TEXT)
	String contentType;
	
	@ObjectField(type = FieldType.LONG)
	Long size;
	
	@ObjectField(type = FieldType.TEXT)
	String hash;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = FileStorageProvider.RESOURCE_KEY)
	FileStorageProvider provider;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String formVariable;
	
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
	
}
