package com.jadaptive.app.entity.template;

import java.util.Objects;

import javax.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.Index;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.UniqueIndex;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.tenant.AbstractSystemObjectDatabaseImpl;
import com.jadaptive.utils.Utils;

@Repository
public class ObjectTemplateRepositoryImpl extends AbstractSystemObjectDatabaseImpl<ObjectTemplate>
		implements ObjectTemplateRepository {

	static Logger log = LoggerFactory.getLogger(ObjectTemplateRepositoryImpl.class);
	
	@Autowired
	private CacheService cacheService; 
	
	public ObjectTemplateRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public Class<ObjectTemplate> getResourceClass() {
		return ObjectTemplate.class;
	}

	private void createTextIndex(ObjectTemplate template, String fieldName) {
		db.createTextIndex(fieldName, template.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}
	
	private void createIndex(ObjectTemplate template, String... fieldNames) {
		db.createIndex(template.getResourceKey(), tenantService.getCurrentTenant().getUuid(), fieldNames);
	}
	
	private void createUniqueIndex(ObjectTemplate template, String... fieldNames) {
		db.createUniqueIndex(template.getResourceKey(), tenantService.getCurrentTenant().getUuid(), fieldNames);
	}

	@Override
	public void createIndexes(ObjectTemplate template, Index[] nonUnique, UniqueIndex[] unique) {
			
		if(Objects.nonNull(nonUnique)) {
			for(Index idx : nonUnique) {
				if(log.isInfoEnabled()) {
					log.info("Creating index on {} for fields {}", template.getName(), Utils.csv(idx.columns()));
				}
				createIndex(template, idx.columns());
			}
		}
		
		if(Objects.nonNull(unique)) {
			for(UniqueIndex idx : unique) {
				if(log.isInfoEnabled()) {
					log.info("Creating unique index on {} for fields {}", template.getName(), Utils.csv(idx.columns()));
				}
				createUniqueIndex(template, idx.columns());
			}
		}
		
		FieldTemplate textIndexField = null;
		for(FieldTemplate field : template.getFields()) {
			if(!field.getResourceKey().equalsIgnoreCase("uuid")) {
				if(field.isUnique()) {
					if(log.isInfoEnabled()) {
						log.info("Creating unique index on {} for field {}", template.getName(), field.getResourceKey());
					}
					createUniqueIndex(template, field.getResourceKey());
				} else {
					if(field.isSearchable() && !field.isTextIndex()) {
						if(log.isInfoEnabled()) {
							log.info("Creating index on {} for field {}", template.getName(), field.getResourceKey());
						}
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
			if(log.isInfoEnabled()) {
				log.info("Creating text index on {} for field {}", template.getName(), textIndexField.getResourceKey());
			}
			createTextIndex(template, textIndexField.getResourceKey());
		}
	}

	@Override
	protected <T extends UUIDEntity> Cache<String, T> getCache(Class<T> obj) {
		return cacheService.getCacheOrCreate("objectTemplates.uuidCache", String.class, obj);
	}		
}

