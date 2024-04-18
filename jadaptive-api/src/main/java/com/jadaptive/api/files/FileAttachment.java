package com.jadaptive.api.files;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = FileAttachment.RESOURCE_KEY)
public class FileAttachment extends AbstractUUIDEntity {

	private static final long serialVersionUID = -2041494617246214405L;

	public static final String RESOURCE_KEY = "fileAttachments";

	@ObjectField(type = FieldType.TEXT)
	String filename;

	@ObjectField(type = FieldType.TEXT)
	String downloadUrl;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
