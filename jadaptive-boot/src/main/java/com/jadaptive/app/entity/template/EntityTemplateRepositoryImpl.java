package com.jadaptive.app.entity.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.Index;
import com.jadaptive.api.template.UniqueIndex;
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
	
	private void createIndex(EntityTemplate template, String... fieldNames) {
		db.createIndex(template.getResourceKey(), tenantService.getCurrentTenant().getUuid(), fieldNames);
	}
	
	private void createUniqueIndex(EntityTemplate template, String... fieldNames) {
		db.createUniqueIndex(template.getResourceKey(), tenantService.getCurrentTenant().getUuid(), fieldNames);
	}

	@Override
	public void createIndexes(EntityTemplate template, Index[] nonUnique, UniqueIndex[] unique) {
			
		if(Objects.nonNull(nonUnique)) {
			for(Index idx : nonUnique) {
				createIndex(template, idx.columns());
			}
		}
		
		if(Objects.nonNull(unique)) {
			for(UniqueIndex idx : unique) {
				createUniqueIndex(template, idx.columns());
			}
		}
		
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

