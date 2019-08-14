package com.jadaptive.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.MongoDatabaseService;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.entity.template.FieldCategory;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.entity.template.ValidationType;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.TenantService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

@Repository
public class EntityRepositoryImpl implements EntityRepository {

	@Autowired
	MongoDatabaseService mongo; 
	
	@Autowired
	TenantService tenantService; 

	@Autowired
	EntityTemplateService templateService; 
	
	@Override
	public Collection<Entity> list(String resourceKey) throws RepositoryException, EntityException {
		
		MongoCollection<Document> collection = getCollection(resourceKey);
		List<Entity> results = new ArrayList<>();
		
		for(Document document : collection.find()) {
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
	
		MongoCollection<Document> collection = getCollection(resourceKey);
		Document e = collection.find(Filters.eq("_id", uuid)).first();
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Uuid %s for entity %s was not found", uuid, resourceKey));
		}
		return buildEntity(resourceKey, e);
	}

	@Override
	public void delete(String resourceKey, String uuid) throws RepositoryException, EntityException {
		MongoCollection<Document> collection = getCollection(resourceKey);
		collection.deleteOne(Filters.eq("_id", uuid));
	}

	@Override
	public void deleteAll(String resourceKey) throws RepositoryException, EntityException {
		
		MongoCollection<Document> collection = getCollection(resourceKey);
		collection.drop();
	
	}

	private MongoCollection<Document> getCollection(Entity e) throws RepositoryException, EntityException {
		return getCollection(e.getResourceKey());
	}
	
	private MongoCollection<Document> getCollection(String resourceKey) throws RepositoryException, EntityException {
		
		MongoDatabase mdb = mongo.getClient().getDatabase(tenantService.getCurrentTenant().getUuid());
		return mdb.getCollection(resourceKey);
	}
	
	@Override
	public void save(Entity entity) throws RepositoryException, EntityException {
		
		MongoCollection<Document> collection = getCollection(entity);
		
		EntityTemplate template = templateService.get(entity.getResourceKey());
		
		validateReferences(template, entity);
		
		if(StringUtils.isBlank(entity.getUuid())) {
			collection.insertOne(entity.getDocument());
		} else {
			collection.replaceOne(Filters.eq("_id", entity.getUuid()), 
					entity.getDocument(), 
					new ReplaceOptions().upsert(true));
		}
	}

	private void validateReferences(EntityTemplate template, Entity entity) {
		
		validateReferences(template.getFields(), entity);
		
		if(!Objects.isNull(template.getCategories())) {
			for(FieldCategory cat : template.getCategories()) {
				validateReferences(cat.getFields(), entity);
			}
		}

	}

	private void validateReferences(Collection<FieldTemplate> fields, Entity entity) {
		if(!Objects.isNull(fields)) {
			for(FieldTemplate t : fields) {
				switch(t.getFieldType()) {
				case OBJECT_REFERENCE:
					if(StringUtils.isNotBlank(entity.getValue(t))) {
						validateEntityExists(entity.getValue(t), t.getValidationValue(ValidationType.OBJECT));
					}
					break;
				case OBJECT_COLLECTION:
					break;
				default:
					break;
				}
			}
		}
	}
	private void validateEntityExists(String uuid, String resourceKey) {
		
		MongoCollection<Document> collection = getCollection(resourceKey);
		Document e = collection.find(Filters.eq("_id", uuid)).first();
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Uuid %s for entity %s was not found", uuid, resourceKey));
		}
	}


}
