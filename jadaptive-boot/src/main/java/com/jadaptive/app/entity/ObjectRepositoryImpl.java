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
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectRepository;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.db.DocumentHelper;

@Repository
public class ObjectRepositoryImpl implements ObjectRepository {

	@Autowired
	DocumentDatabase db;
	
	@Autowired
	TenantService tenantService; 

	@Autowired
	ObjectTemplateRepository templateRepository; 
	
	@Override
	public Collection<AbstractObject> list(ObjectTemplate def, SearchField... fields) throws RepositoryException, ObjectException {
		
		List<AbstractObject> results = new ArrayList<>();
		
		for(Document document : db.list(def.getResourceKey(), tenantService.getCurrentTenant().getUuid(), fields)) {
			results.add(buildEntity(def, document));
		}
		
		return results;
	}
	
	@Override
	public Collection<AbstractObject> personal(ObjectTemplate def, User user) throws RepositoryException, ObjectException {
		
		List<AbstractObject> results = new ArrayList<>();
		
		for(Document document : db.list(def.getResourceKey(), tenantService.getCurrentTenant().getUuid(), SearchField.eq("ownerUUID", user.getUuid()))) {
			results.add(buildEntity(def, document));
		}
		
		return results;
	}

	private AbstractObject buildEntity(ObjectTemplate def, Document document) {
		MongoEntity e = new MongoEntity(def.getResourceKey(), document);
		e.setUuid(document.getString("_id"));
		e.setHidden(document.getBoolean("hidden"));
		e.setSystem(document.getBoolean("system"));
		return e;
	}
	
	@Override
	public AbstractObject getById(ObjectTemplate def, String value) throws RepositoryException, ObjectException {
		
		List<SearchField> search = new ArrayList<>();
		search.add(SearchField.eq("uuid", value));
		for(FieldTemplate field : def.getFields()) {
			if(field.isAlternativeId()) {
				search.add(SearchField.eq(field.getResourceKey(), DocumentHelper.fromString(field, value)));
			}
		}
		Document document = db.get(def.getResourceKey(),
				tenantService.getCurrentTenant().getUuid(), 
				SearchField.or(search.toArray(new SearchField[0])));
		if(Objects.isNull(document)) {
			throw new ObjectNotFoundException(String.format("No document for resource %s with value %s", def.getResourceKey(), value));
		}
		return buildEntity(def, document);
	}

	@Override
	public void deleteByUUID(ObjectTemplate def, String uuid) throws RepositoryException, ObjectException {
		db.deleteByUUID(uuid, def.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void deleteAll(ObjectTemplate def) throws RepositoryException, ObjectException {
		db.dropCollection(def.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}
	
	@Override
	public void deleteByUUIDOrAltId(ObjectTemplate def, String value) throws RepositoryException, ObjectException {
		List<SearchField> search = new ArrayList<>();
		search.add(SearchField.eq("uuid", value));
		for(FieldTemplate field : def.getFields()) {
			if(field.isAlternativeId()) {
				search.add(SearchField.eq(field.getResourceKey(), DocumentHelper.fromString(field, value)));
			}
		}
		db.delete(def.getResourceKey(), tenantService.getCurrentTenant().getUuid(), 
				SearchField.or(search.toArray(new SearchField[0])));
	}
	
	@Override
	public String save(AbstractObject entity) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateRepository.get(SearchField.eq("resourceKey", entity.getResourceKey()));
		
		validateReferences(template, entity);
		
		Document doc = new Document(entity.getDocument());
		db.insertOrUpdate(entity, doc, entity.getResourceKey(), tenantService.getCurrentTenant().getUuid());
		return doc.getString("_id");
	}

	private void validateReferences(ObjectTemplate template, AbstractObject entity) {
		validateReferences(template.getFields(), entity);
	}

	private void validateReferences(Collection<FieldTemplate> fields, AbstractObject entity) {
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
	public Collection<AbstractObject> table(ObjectTemplate def, String field, String search, int offset, int limit) {
		List<AbstractObject> results = new ArrayList<>();
		
		for(Document document : db.table(def.getResourceKey(), field, search, tenantService.getCurrentTenant().getUuid(), offset, limit)) {
			results.add(buildEntity(def, document));
		}
		
		return results;
	}
	
	@Override
	public long count(ObjectTemplate def) {
		return db.count(def.getResourceKey(), tenantService.getCurrentTenant().getUuid());
	}
	
	@Override
	public long count(ObjectTemplate def, String searchField, String searchValue) {
		return db.count(def.getResourceKey(), searchField, searchValue, tenantService.getCurrentTenant().getUuid());
	}


}
