package com.jadaptive.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.entity.template.ValidationType;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.TenantService;

@Repository
public class EntityRepositoryImpl implements EntityRepository {

	@Autowired
	DocumentDatabase db;
	
	@Autowired
	TenantService tenantService; 

	@Autowired
	EntityTemplateService templateService; 
	
	@Override
	public Collection<Entity> list(String resourceKey) throws RepositoryException, EntityException {
		
		List<Entity> results = new ArrayList<>();
		
		for(Document document : db.list(resourceKey, tenantService.getCurrentTenant().getUuid())) {
			results.add(buildEntity(resourceKey, document));
		}
		
		return results;
	}

	private Entity buildEntity(String resourceKey, Document document) {
		Entity e = new Entity(resourceKey, document);
		e.setUuid(document.getString("_id"));
		e.setHidden(Boolean.valueOf(document.getString("hidden")));
		e.setSystem(Boolean.valueOf(document.getString("system")));
		return e;
	}
	
	@Override
	public Entity get(String uuid, String resourceKey) throws RepositoryException, EntityException {
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
	public void save(Entity entity) throws RepositoryException, EntityException {
		
		EntityTemplate template = templateService.get(entity.getResourceKey());
		
		validateReferences(template, entity);
		
		db.insertOrUpdate(entity, entity.getDocument(), entity.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}

	private void validateReferences(EntityTemplate template, Entity entity) {
		validateReferences(template.getFields(), entity);
	}

	private void validateReferences(Collection<FieldTemplate> fields, Entity entity) {
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
	public Collection<Entity> table(String resourceKey, int offset, int limit) {
		List<Entity> results = new ArrayList<>();
		
		for(Document document : db.table(resourceKey, tenantService.getCurrentTenant().getUuid(), offset, limit)) {
			results.add(buildEntity(resourceKey, document));
		}
		
		return results;
	}

	@Override
	public long count(String resourceKey) {
		return db.count(resourceKey, tenantService.getCurrentTenant().getUuid());
	}


}
