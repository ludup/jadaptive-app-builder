package com.jadaptive.app.entity.template;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.Index;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.UniqueIndex;
import com.jadaptive.api.template.ValidationType;
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
	
	@Override
	public Collection<ObjectTemplate> findReferences(ObjectTemplate template) {
		return searchObjects(SearchField.all("fields.validators.type", ValidationType.RESOURCE_KEY.name()),
			SearchField.all("fields.validators.value", template.getResourceKey()));
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

	private String getIndexName(String type, String... fieldNames) {
		return type + "_" + StringUtils.join(fieldNames, "_");
	}
	
	@Override
	public void createIndexes(ObjectTemplate template, Index[] nonUnique, UniqueIndex[] unique, boolean newSchema) {
			
		if(Boolean.getBoolean("jadaptive.rebuildIndexes") || newSchema || calculateIndexes(template, nonUnique, unique)) {
		
			db.dropIndexes(template.getResourceKey(), tenantService.getCurrentTenant().getUuid());
		
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
	}

	private boolean calculateIndexes(ObjectTemplate template, Index[] nonUnique, UniqueIndex[] unique) {
		
		Set<String> indexNames = new TreeSet<>(db.getIndexNames(template.getResourceKey(), tenantService.getCurrentTenant().getUuid()));
		Set<String> newIndexNames = new TreeSet<>();
		
		for(Index idx : nonUnique) {
			newIndexNames.add(getIndexName("index", idx.columns()));
		}
		
		for(UniqueIndex idx : unique) {
			newIndexNames.add(getIndexName("unique", idx.columns()));
		}
		
		FieldTemplate textIndexField = null;
		for(FieldTemplate field : template.getFields()) {
			if(!field.getResourceKey().equalsIgnoreCase("uuid")) {
				if(field.isUnique()) {
					newIndexNames.add(getIndexName("unique", field.getResourceKey()));
				} else {
					if(field.isSearchable() && !field.isTextIndex()) {
						newIndexNames.add(getIndexName("index", field.getResourceKey()));
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
			newIndexNames.add(getIndexName("text", textIndexField.getResourceKey()));
		}
		
		if(newIndexNames.size()!= indexNames.size()) {
			return true;
		}
		
		Set<String> compare = new TreeSet<>(indexNames);
		
		indexNames.removeAll(newIndexNames);
		newIndexNames.removeAll(compare);
		return !(indexNames.isEmpty() && newIndexNames.isEmpty());
	}

	@Override
	protected <T extends UUIDEntity> Map<String, T> getCache(Class<T> obj) {
		return cacheService.getCacheOrCreate("objectTemplates.uuidCache", String.class, obj);
	}		
}

