package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityRepository;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.repository.RepositoryException;

@Repository
public class EntityRepositoryImpl implements EntityRepository<MongoEntity> {

	@Autowired
	DocumentDatabase db;
	
	@Autowired
	TenantService tenantService; 

	@Autowired
	EntityTemplateService templateService; 
	
	@Override
	public Collection<MongoEntity> list(String resourceKey) throws RepositoryException, EntityException {
		
		List<MongoEntity> results = new ArrayList<>();
		
		for(Document document : db.list(resourceKey, tenantService.getCurrentTenant().getUuid())) {
			results.add(buildEntity(resourceKey, document));
		}
		
		return results;
	}

	private MongoEntity buildEntity(String resourceKey, Document document) {
		MongoEntity e = new MongoEntity(resourceKey, document);
		e.setUuid(document.getString("_id"));
		e.setHidden(Boolean.valueOf(document.getString("hidden")));
		e.setSystem(Boolean.valueOf(document.getString("system")));
		return e;
	}
	
	@Override
	public MongoEntity get(String uuid, String resourceKey) throws RepositoryException, EntityException {
		return buildEntity(resourceKey, db.get(uuid, resourceKey, tenantService.getCurrentTenant().getUuid()));
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, EntityException {
		db.delete(uuid, resourceKey, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void deleteAll(String resourceKey) throws RepositoryException, EntityException {
		
		db.dropCollection(resourceKey, tenantService.getCurrentTenant().getUuid());
	
	}
	
	@Override
	public void save(MongoEntity entity) throws RepositoryException, EntityException {
		
		EntityTemplate template = templateService.get(entity.getResourceKey());
		
		validateReferences(template, entity);
		
		db.insertOrUpdate(entity, entity.getDocument(), entity.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}

	private void validateReferences(EntityTemplate template, MongoEntity entity) {
		validateReferences(template.getFields(), entity);
	}

	private void validateReferences(Collection<FieldTemplate> fields, MongoEntity entity) {
		if(!Objects.isNull(fields)) {
			for(FieldTemplate t : fields) {
				switch(t.getFieldType()) {
				case OBJECT_REFERENCE:
					if(StringUtils.isNotBlank(entity.getValue(t))) {
						validateEntityExists(entity.getValue(t), t.getValidationValue(ValidationType.OBJECT_TYPE));
					}
					break;
				case OBJECT_EMBEDDED:
					validateReferences(templateService.get(
							t.getValidationValue(ValidationType.OBJECT_TYPE)), 
							entity.getChild(t));
					break;
				default:
					break;
				}
			}
		}
	}
	
	private void validateEntityExists(String uuid, String resourceKey) {
		db.getFirst(uuid, resourceKey, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Collection<MongoEntity> table(String resourceKey, String field, String search, int offset, int limit) {
		List<MongoEntity> results = new ArrayList<>();
		
		for(Document document : db.table(resourceKey, field, search, tenantService.getCurrentTenant().getUuid(), offset, limit)) {
			results.add(buildEntity(resourceKey, document));
		}
		
		return results;
	}

	@Override
	public long count(String resourceKey) {
		return db.count(resourceKey, tenantService.getCurrentTenant().getUuid());
	}
	
	@Override
	public long count(String resourceKey, String searchField, String searchValue) {
		return db.count(resourceKey, searchField, searchValue, tenantService.getCurrentTenant().getUuid());
	}


}
