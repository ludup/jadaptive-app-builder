package com.jadaptive.app.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bson.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.api.entity.AbstractEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.app.repository.AbstractUUIDEntity;

@JsonDeserialize(using=EntityDeserializer.class)
@JsonSerialize(using=EntitySerializer.class)
public class MongoEntity extends AbstractUUIDEntity implements AbstractEntity {

	MongoEntity parent;
	Map<String,MongoEntity> children = new HashMap<>();
	String resourceKey;
	Document document;
	
	public MongoEntity() {	
	}
	
	public MongoEntity(String resourceKey, Document document) {
		this(null, resourceKey, document);
		for(Entry<String,Object> entry : document.entrySet()) {
			if(entry.getValue() instanceof Document) {
				new MongoEntity(this, entry.getKey(), (Document)entry.getValue());
			}
		}
	}
	
	public MongoEntity(MongoEntity parent, String resourceKey, Document document) {
		this.parent = parent;
		this.resourceKey = resourceKey;
		this.document = document;
		
		if(!Objects.isNull(parent)) {
			parent.addChild(this);
		}
	}
	
	private void addChild(MongoEntity e) {
		children.put(e.getResourceKey(), e);
		document.put(e.getResourceKey(), e.getDocument());
	}

	@Override
	public MongoEntity getChild(FieldTemplate c) {
		return children.get(c.getResourceKey());
	}

	@Override
	public String getResourceKey() {
		return resourceKey;
	}

	@Override
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	@Override
	public void setUuid(String uuid) {
		document.put("_id",  uuid);
		super.setUuid(uuid);
	}

	@Override
	public void setSystem(Boolean system) {
		document.put("system",  String.valueOf(system));
		super.setSystem(system);
	}

	@Override
	public void setHidden(Boolean hidden) {
		document.put("hidden",  String.valueOf(hidden));
		super.setHidden(hidden);
	}

	@Override
	public String getValue(String fieldName) {
		return document.get(fieldName, "");
	}
	
	@Override
	public String getValue(FieldTemplate t) {
		switch(t.getFieldType()) {
		case BOOL:
		case DECIMAL:
		case NUMBER:
		case TEXT:
		case TEXT_AREA:
		case COUNTRY:
		default:
			return document.get(t.getResourceKey(), t.getDefaultValue());
		}
	}

	@Override
	public void setValue(FieldTemplate t, String value) {
		document.put(t.getResourceKey(), value);
	}
}
