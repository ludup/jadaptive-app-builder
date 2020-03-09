package com.jadaptive.app.entity.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.tenant.AbstractTenantAwareObjectDatabaseImpl;

@Repository
public class EntityTemplateRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<EntityTemplate>
		implements EntityTemplateRepository {

	Map<String,EntityTemplate> cache = new HashMap<>();
	public EntityTemplateRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public Class<EntityTemplate> getResourceClass() {
		return EntityTemplate.class;
	}

	private void createTextIndex(EntityTemplate template, String fieldName) {
		db.createTextIndex(fieldName, template.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}
	
	private void createIndex(EntityTemplate template, String fieldName) {
		db.createIndex(fieldName, template.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}
	
	private void createUniqueIndex(EntityTemplate template, String fieldName) {
		db.createUniqueIndex(fieldName, template.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void createIndexes(EntityTemplate template) {
			
		FieldTemplate textIndexField = null;
		for(FieldTemplate field : template.getFields()) {
			if(!field.getResourceKey().equalsIgnoreCase("uuid")) {
				if(field.isUnique()) {
					createUniqueIndex(template, field.getResourceKey());
				} else {
					if(field.isSearchable() && !field.isTextIndex()) {
						createIndex(template, field.getResourceKey());
					} else if(field.isTextIndex()) {
						if(Objects.nonNull(textIndexField)) {
							throw new IllegalStateException(
									String.format("Invalid index specification; multiple text index fields declared on %s",
											template.getName()));
						}
						textIndexField = field;
					}
				}
				
			}
		}
		if(Objects.nonNull(textIndexField)) {
			createTextIndex(template, textIndexField.getResourceKey());
		}
	}		
}

