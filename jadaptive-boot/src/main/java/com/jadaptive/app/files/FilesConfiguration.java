package com.jadaptive.app.files;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.files.FileStorageProvider;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.ui.pages.config.ConfigurationItem;

@ObjectDefinition(resourceKey = FilesConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON, system = true)
@ConfigurationItem(bundle = FilesConfiguration.RESOURCE_KEY, icon = "fa-files", resourceKey = FilesConfiguration.RESOURCE_KEY)
public class FilesConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -6094738664134766407L;
	
	public static final String RESOURCE_KEY = "fileConfiguration";
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = FileStorageProvider.RESOURCE_KEY)
	@ObjectView(value = "", renderer = FieldRenderer.DROPDOWN)
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