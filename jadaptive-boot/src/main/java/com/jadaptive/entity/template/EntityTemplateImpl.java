package com.jadaptive.entity.template;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.jadaptive.entity.repository.DataSourceEntity;
import com.jadaptive.entity.repository.EntityTemplate;
import com.jadaptive.entity.repository.EntityType;
import com.jadaptive.entity.repository.NamedUUIDEntityImpl;

public class EntityTemplateImpl extends NamedUUIDEntityImpl implements EntityTemplate, DataSourceEntity {

	EntityType type;
	Set<FieldTemplateImpl> fields;
	Set<FieldCategoryImpl> categories;
	
	@Override
	public EntityType getType() {
		return type;
	}
	
	@Override
	public void setType(EntityType type) {
		this.type = type;
	}
	
	@Override
	public Set<FieldCategoryImpl> getCategories() {
		return categories;
	}
	
	@Override
	public void setCategories(Set<FieldCategoryImpl> categories) {
		this.categories = categories;
	}

	public Set<FieldTemplateImpl> getFields() {
		return fields;
	}

	public void setFields(Set<FieldTemplateImpl> fields) {
		this.fields = fields;
	}

	@Override
	public void store(Map<String,Map<String, String>> properties) throws ParseException {
		
		Map<String,String> m = new HashMap<>();
		
		super.toMap(m);

		m.put("type", type.name());
		StringBuffer index = new StringBuffer();
		if(!Objects.isNull(fields)) {
			for(FieldTemplateImpl field : fields) {
				if(index.length() > 0) {
					index.append(",");
				}
				index.append(field.getUuid());
				Map<String,String> fm = new HashMap<>();
				field.toMap(fm);
				properties.put(field.getUuid(), fm);
			}
		}
		
		m.put("fields", index.toString());
		
		index.setLength(0);
		if(!Objects.isNull(categories)) {
			for(FieldCategoryImpl cat : categories) {
				if(index.length() > 0) {
					index.append(",");
				}
				index.append(cat.getUuid());
				cat.toMap(properties);
			}
		}
		
		m.put("categories", index.toString());
		
		properties.put(getUuid(), m);
	}

	@Override
	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String,String> m = properties.get(uuid);
		
		super.fromMap(m);
		
		this.type = EntityType.valueOf(m.get("type"));
		this.fields = new HashSet<>();
		for(String fieldUuid : m.get("fields").split(",")) {
			FieldTemplateImpl f = new FieldTemplateImpl();
			f.fromMap(fieldUuid, properties.get(fieldUuid));
			this.fields.add(f);
		}
		categories = new HashSet<>();
		for(String catUuid : m.get("categories").split(",")) {
			FieldCategoryImpl c = new FieldCategoryImpl();
			c.fromMap(catUuid, properties);
			categories.add(c);
		}
	}
}
