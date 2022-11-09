package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class MongoEntity  extends AbstractUUIDEntity implements AbstractObject {

	private static final long serialVersionUID = 7834313955696773158L;
	AbstractObject parent;
	Map<String,AbstractObject> children = new HashMap<>();
	Document document;
	String contentHash;
	String resourceKey;
	
	public MongoEntity(Map<String,Object> document) {	
		this((String)document.get("resourceKey"), document);
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
	
	@SuppressWarnings("unchecked")
	public MongoEntity(AbstractObject parent, String resourceKey, Map<String,Object> document) {
		this.parent = parent;
		this.resourceKey = resourceKey;
		this.document = new Document(document);
		if(!this.document.containsKey("resourceKey")) {
			this.document.put("resourceKey", resourceKey);
		}
		String uuid = (String) document.getOrDefault("_id", null);
		if(Objects.nonNull(uuid)) {
			setUuid(uuid);
		}
		setSystem((Boolean)document.getOrDefault("system", Boolean.FALSE));
		setHidden((Boolean)document.getOrDefault("hidden", Boolean.FALSE));
		if(!Objects.isNull(parent)) {
			parent.addChild(resourceKey, this);
		}
		for(Map.Entry<String,Object> entry : document.entrySet()) {
			if(entry.getValue() instanceof Document) {
				new MongoEntity(this, entry.getKey(), (Map<String,Object>)entry.getValue());
			}
		}
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	@Override
	public void addChild(String resourceKey, AbstractObject e) {
		children.put(resourceKey, e);
		document.put(resourceKey, e.getDocument());
	}

	@Override
	public AbstractObject getChild(FieldTemplate c) {
		return children.get(c.getResourceKey());
	}

	@Override
	public String getResourceKey() {
		return document.getString("resourceKey");
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
		if(fieldName.equals("uuid")) {
			return getUuid();
		}
		return document.get(fieldName);
	}
	
	@Override
	public Object getValue(FieldTemplate t) {
		switch(t.getFieldType()) {
		case OBJECT_EMBEDDED:
			throw new IllegalArgumentException("Use getChild to object embedded object");
		default:
			Object value = document.get(t.getResourceKey());
			if(value==null && t.getFieldType().canDefault()) {
				value = t.getDefaultValue();
			}
			return value;
		}
	}
	
	public Collection<String> getCollection(String fieldName) {
		Collection<String> results = document.getList(fieldName, String.class);
		return Objects.nonNull(results) ? results : Collections.emptyList();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValue(FieldTemplate t, Object value) {
		if(value instanceof Map) {
			new MongoEntity(this, t.getResourceKey(), (Map)value);
		} else {
			document.put(t.getResourceKey(), value);
		}
	}

	public void setValue(FieldTemplate t, List<Object> values) {
		document.put(t.getResourceKey(), values);
	}

	@SuppressWarnings("unchecked")
	public Collection<AbstractObject> getObjectCollection(String fieldName) {
		List<AbstractObject> tmp = new ArrayList<>();
		if(document.containsKey(fieldName)) {
			for(Map<String,Object> child :document.getList(fieldName, Map.class) ) {
				tmp.add(new MongoEntity((String)child.get("resourceKey"), child));
			}
		}
		return tmp;
	}
	
	@Override
	public void removeCollectionObject(String fieldName, AbstractObject e) {
		document.getList(fieldName, Map.class).removeIf(entries->entries.get("_id").equals(e.getUuid()));
	}
	
	@Override
	public void addCollectionObject(String fieldName, AbstractObject e) {
		document.getList(fieldName, Map.class).add(e.getDocument());
	}

	public void setValue(String key, String value) {
		document.put(key, value);
	}

	@Override
	public Map<String,AbstractObject> getChildren() {
		return Collections.unmodifiableMap(children);
	}
}
