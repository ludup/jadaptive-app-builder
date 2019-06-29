package com.jadaptive.entity.template;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.datasource.DataSourceEntity;
import com.jadaptive.entity.EntityType;
import com.jadaptive.repository.NamedUUIDEntity;

public class EntityTemplate extends NamedUUIDEntity implements DataSourceEntity {

	EntityType type;
	Set<FieldTemplate> fields;
	Set<FieldCategory> categories;
	
	public EntityType getType() {
		return type;
	}
	
	public void setType(EntityType type) {
		this.type = type;
	}
	
	public Set<FieldCategory> getCategories() {
		return categories;
	}
	
	@JsonIgnore
	public Map<String,FieldCategory> getCategoriesMap() {
		Map<String,FieldCategory> tmp = new HashMap<>();
		for(FieldCategory c : categories) {
			tmp.put(c.getResourceKey(), c);
		}
		return tmp;
	}
	
	public void setCategories(Set<FieldCategory> categories) {
		this.categories = categories;
	}

	public Set<FieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Set<FieldTemplate> fields) {
		this.fields = fields;
	}

	@Override
	public void store(Map<String,Map<String, String>> properties) throws ParseException {
		
		Map<String,String> m = new HashMap<>();
		
		super.toMap(m);

		m.put("type", type.name());
		StringBuffer index = new StringBuffer();
		if(!Objects.isNull(fields)) {
			for(FieldTemplate field : fields) {
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
			for(FieldCategory cat : categories) {
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
		String fieldIndex = m.get("fields");
		if(StringUtils.isNotBlank(fieldIndex)) {
			for(String fieldUuid : fieldIndex.split(",")) {
				FieldTemplate f = new FieldTemplate();
				f.fromMap(fieldUuid, properties.get(fieldUuid));
				this.fields.add(f);
			}
		}
		categories = new HashSet<>();
		String categoryIndex = m.get("categories");
		if(StringUtils.isNotBlank(categoryIndex)) {
			for(String catUuid : categoryIndex.split(",")) {
				FieldCategory c = new FieldCategory();
				c.fromMap(catUuid, properties);
				categories.add(c);
			}
		}
	}
}
