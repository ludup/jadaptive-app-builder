package com.jadaptive.api.files;

import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = FileStorageProvider.RESOURCE_KEY)
public class FileStorageProvider extends NamedUUIDEntity {

	private static final long serialVersionUID = -8458346896900750771L;
	public static final String RESOURCE_KEY = "fileStorageProvider";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
