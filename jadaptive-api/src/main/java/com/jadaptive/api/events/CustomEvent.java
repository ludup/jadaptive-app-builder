package com.jadaptive.api.events;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = "events", type = ObjectType.COLLECTION)
public class CustomEvent extends UUIDEntity {

	private static final long serialVersionUID = -5647655839475687686L;

	@ObjectField(searchable = true,
			type = FieldType.TEXT)
	String resourceKey;
		
	@ObjectField(searchable = true,
			type = FieldType.BOOL)
	boolean success;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String error;
	
	@ObjectField(searchable = true,
			type = FieldType.TIMESTAMP)
	Date timestamp = new Date();
	
	public CustomEvent(String resourceKey) {
		this.resourceKey = resourceKey;
		this.success = true;
	}
	
	public CustomEvent(String resourceKey, Throwable e) {
		this.resourceKey = resourceKey;
		this.success = false;
		this.error = generateExceptionText(e);
	}

	protected void markSuccess() {
		this.success = true;
	}
	
	public Boolean isSystem() {
		return true;
	}
	
	public Boolean isHidden() {
		return false;
	}
	
	private String generateExceptionText(Throwable e) {
		try(StringWriter w = new StringWriter()) {
			e.printStackTrace(new PrintWriter(w));
			return w.toString();
		} catch (IOException e1) {
			throw new IllegalStateException(e1.getMessage(), e1);
		}
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public boolean isSuccess() {
		return success;
	}
	
	public String getError() {
		return error;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
