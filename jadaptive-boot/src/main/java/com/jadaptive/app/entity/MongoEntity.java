package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldTemplate;

@JsonDeserialize(using=AbstractObjectDeserializer.class)
@JsonSerialize(using=AbstractObjectSerializer.class)
public class MongoEntity extends AbstractUUIDEntity implements AbstractObject {

	private static final long serialVersionUID = 7834313955696773158L;
	
	AbstractObject parent;
	Map<String,AbstractObject> children = new HashMap<>();
	String resourceKey;
	Document document;
	
	public MongoEntity() {	
		this.document = new Document();
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
	
	public MongoEntity(AbstractObject parent, String resourceKey, Map<String,Object> document) {
		this.parent = parent;
		this.resourceKey = resourceKey;
		this.document = new Document(document);
		
		if(!Objects.isNull(parent)) {
			parent.addChild(this);
		}
	}
	
	@Override
	public void addChild(AbstractObject e) {
		children.put(e.getResourceKey(), e);
		document.put(e.getResourceKey(), e.getDocument());
	}

	@Override
	public AbstractObject getChild(FieldTemplate c) {
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
		document.put("system",  system);
		super.setSystem(system);
	}

	@Override
	public void setHidden(Boolean hidden) {
		document.put("hidden",  hidden);
		super.setHidden(hidden);
	}

	@Override
	public Object getValue(String fieldName) {
		return document.get(fieldName);
	}
	
	@Override
	public Object getValue(FieldTemplate t) {
		switch(t.getFieldType()) {
		case OBJECT_EMBEDDED:
			throw new IllegalArgumentException("Use getChild to object embedded object");
		default:
			Object value = document.get(t.getResourceKey());
			if(value==null) {
				value = t.getDefaultValue();
			}
			return value;
		}
	}
	
	public Collection<String> getCollection(String fieldName) {
		return document.getList(fieldName, String.class);
	}
	
//	public Collection<?> getReferenceCollection(String fieldName) {
//		return document.getList(fieldName, Map.class);
//	}

	@Override
	public void setValue(FieldTemplate t, Object value) {
		document.put(t.getResourceKey(), value);
	}

	public void setValue(FieldTemplate t, List<Object> values) {
		document.put(t.getResourceKey(), values);
	}

	@SuppressWarnings("unchecked")
	public Collection<AbstractObject> getObjectCollection(String fieldName) {
		List<AbstractObject> tmp = new ArrayList<>();
		for(Map<String,Object> child : document.getList(fieldName, Map.class)) {
			tmp.add(new MongoEntity(fieldName, child));
		}
		return tmp;
	}
}
