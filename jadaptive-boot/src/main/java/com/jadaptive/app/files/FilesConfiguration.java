package com.jadaptive.app.files;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.files.FileStorageProvider;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = FilesConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
public class FilesConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -6094738664134766407L;
	
	public static final String RESOURCE_KEY = "fileConfiguration";
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = FileStorageProvider.RESOURCE_KEY)
	FileStorageProvider storageProvider;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public FileStorageProvider getStorageProvider() {
		return storageProvider;
	}

	public void setStorageProvider(FileStorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

}