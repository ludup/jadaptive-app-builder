package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractEntity;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.entity.EntityRepository;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.DocumentDatabase;

@Repository
public class EntityRepositoryImpl implements EntityRepository {

	@Autowired
	DocumentDatabase db;
	
	@Autowired
	TenantService tenantService; 

	@Autowired
	EntityTemplateRepository templateRepository; 
	
	@Override
	public Collection<AbstractEntity> list(String resourceKey, SearchField... fields) throws RepositoryException, EntityException {
		
		List<AbstractEntity> results = new ArrayList<>();
		
		for(Document document : db.list(resourceKey, tenantService.getCurrentTenant().getUuid(), fields)) {
			results.add(buildEntity(resourceKey, document));
		}
		
		return results;
	}

	private AbstractEntity buildEntity(String resourceKey, Document document) {
		MongoEntity e = new MongoEntity(resourceKey, document);
		e.setUuid(document.getString("_id"));
		e.setHidden(document.getBoolean("hidden"));
		e.setSystem(document.getBoolean("system"));
		return e;
	}
	
	@Override
	public AbstractEntity get(String uuid, String resourceKey) throws RepositoryException, EntityException {
		Document document = db.get(uuid, resourceKey, tenantService.getCurrentTenant().getUuid());
		if(Objects.isNull(document)) {
			throw new EntityNotFoundException(String.format("No document for resource %s with uuid %s", resourceKey, uuid));
		}
		return buildEntity(resourceKey, document);
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
	public String save(AbstractEntity entity) throws RepositoryException, EntityException {
		
		EntityTemplate template = templateRepository.get(SearchField.eq("resourceKey", entity.getResourceKey()));
		
		validateReferences(template, entity);
		
		Document doc = new Document(entity.getDocument());
		db.insertOrUpdate(entity, doc, entity.getResourceKey(), tenantService.getCurrentTenant().getUuid());
		return doc.getString("_id");
	}

	private void validateReferences(EntityTemplate template, AbstractEntity entity) {
		validateReferences(template.getFields(), entity);
	}

	private void validateReferences(Collection<FieldTemplate> fields, AbstractEntity entity) {
		if(!Objects.isNull(fields)) {
			for(FieldTemplate t : fields) {
				switch(t.getFieldType()) {
				case OBJECT_REFERENCE:
					if(StringUtils.isNotBlank((String)entity.getValue(t))) {
						validateEntityExists((String)entity.getValue(t), t.getValidationValue(ValidationType.OBJECT_TYPE));
					}
					break;
				case OBJECT_EMBEDDED:
					validateReferences(templateRepository.get(
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
	public Collection<AbstractEntity> table(String resourceKey, String field, String search, int offset, int limit) {
		List<AbstractEntity> results = new ArrayList<>();
		
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
