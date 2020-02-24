package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.api.entity.AbstractEntity;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldTemplate;

@JsonDeserialize(using=EntityDeserializer.class)
@JsonSerialize(using=EntitySerializer.class)
public class MongoEntity extends AbstractUUIDEntity implements AbstractEntity {

	MongoEntity parent;
	Map<String,MongoEntity> children = new HashMap<>();
	String resourceKey;
	Document document;
	
	public MongoEntity() {	
	}
	
	public MongoEntity(String resourceKey) {
		this(resourceKey, new Document());
	}
	
	@SuppressWarnings("unchecked")
	public MongoEntity(String resourceKey, Map<String,Object> document) {
		this(null, resourceKey, document);
		for(Map.Entry<String,Object> entry : document.entrySet()) {
			if(entry.getValue() instanceof Document) {
				new MongoEntity(this, entry.getKey(), (Map<String,Object>)entry.getValue());
			}
		}
	}
	
	public MongoEntity(MongoEntity parent, String resourceKey, Map<String,Object> document) {
		this.parent = parent;
		this.resourceKey = resourceKey;
		this.document = new Document(document);
		
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

	public Map<String,Object> getDocument() {
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
		return StringUtils.defaultString((String) document.get(fieldName), "");
	}
	
	@Override
	public String getValue(FieldTemplate t) {
		switch(t.getFieldType()) {
		case OBJECT_EMBEDDED:
			throw new IllegalArgumentException("Use getChild to object embedded object");
		default:
			return StringUtils.defaultString((String) document.get(t.getResourceKey()), t.getDefaultValue());
		}
	}
	
	public Collection<String> getCollection(String fieldName) {
		return document.getList(fieldName, String.class);
	}

	@Override
	public void setValue(FieldTemplate t, String value) {
		document.put(t.getResourceKey(), value);
	}

	public void setValue(FieldTemplate t, List<Object> values) {
		document.put(t.getResourceKey(), values);
	}

	@SuppressWarnings("unchecked")
	public Collection<MongoEntity> getObjectCollection(String fieldName) {
		List<MongoEntity> tmp = new ArrayList<>();
		for(Map<String,Object> child : document.getList(fieldName, Map.class)) {
			tmp.add(new MongoEntity(fieldName, child));
		}
		return tmp;
	}
}
