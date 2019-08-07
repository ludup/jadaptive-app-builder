package com.jadaptive.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bson.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.entity.template.FieldCategory;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.repository.AbstractUUIDEntity;

@JsonSerialize(using=EntitySerializer.class)
@JsonDeserialize(using=EntityDeserializer.class)
public class Entity extends AbstractUUIDEntity {

	Entity parent;
	Map<String,Entity> children = new HashMap<>();
	String resourceKey;
	Document document;
	
	public Entity() {	
	}
	
	public Entity(String resourceKey, Document document) {
		this(null, resourceKey, document);
		for(Entry<String,Object> entry : document.entrySet()) {
			if(entry.getValue() instanceof Document) {
				children.put(entry.getKey(), new Entity(this, resourceKey, (Document)entry.getValue()));
			}
		}
	}
	
	public Entity(Entity parent, String resourceKey, Document document) {
		this.parent = parent;
		this.resourceKey = resourceKey;
		this.document = document;
		
		if(!Objects.isNull(parent)) {
			parent.addChild(this);
		}
	}
	
	private void addChild(Entity e) {
		children.put(e.getResourceKey(), e);
		document.put(e.getResourceKey(), e.getDocument());
	}
	
	public Entity getChild(FieldCategory c) {
		return children.get(c.getResourceKey());
	}

	public String getResourceKey() {
		return resourceKey;
	}

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

	public String getValue(FieldTemplate t) {
		switch(t.getFieldType()) {
		case CHECKBOX:
		case DECIMAL:
		case NUMBER:
		case TEXT:
		case TEXT_AREA:
		case COUNTRY:
		default:
			return document.get(t.getResourceKey(), t.getDefaultValue());
		}
	}

	public void setValue(FieldTemplate t, String value) {
		document.put(t.getResourceKey(), value);
	}
}
