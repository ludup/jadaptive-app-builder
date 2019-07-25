package com.jadaptive.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.MongoDatabaseService;
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
		return buildEntity(resourceKey, collection.find(Filters.eq("_id", uuid)).first());
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
		
		if(StringUtils.isBlank(entity.getUuid())) {
			collection.insertOne(entity.getDocument());
		} else {
			collection.replaceOne(Filters.eq("_id", entity.getUuid()), 
					entity.getDocument(), 
					new ReplaceOptions().upsert(true));
		}
	}


}
