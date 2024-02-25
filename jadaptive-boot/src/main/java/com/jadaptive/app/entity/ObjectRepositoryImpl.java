package com.jadaptive.app.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.db.MongoEntity;

@Repository
public class ObjectRepositoryImpl implements ObjectRepository {

	static Logger log = LoggerFactory.getLogger(ObjectRepositoryImpl.class);
	
	@Autowired
	DocumentDatabase db;
	
	@Autowired
	TenantService tenantService; 

	@Autowired
	ObjectTemplateRepository templateRepository; 
	
	@Autowired
	TemplateService templateService;
	
	@Override
	public Iterable<AbstractObject> list(ObjectTemplate def, SearchField... fields) throws RepositoryException, ObjectException {
		return new ObjectIterable(def, db.list(def.getCollectionKey(), getDatabase(def), fields));
	}
	
	@Override
	public Collection<AbstractObject> personal(ObjectTemplate def, User user) throws RepositoryException, ObjectException {
		
		List<AbstractObject> results = new ArrayList<>();
		
		for(Document document : db.list(def.getCollectionKey(), getDatabase(def), SearchField.eq("ownerUUID", user.getUuid()))) {
			results.add(buildEntity(def, document));
		}
		
		return results;
	}

	private AbstractObject buildEntity(ObjectTemplate def, Document document) {
		MongoEntity e = new MongoEntity(document);
		e.setUuid(document.getString("_id"));
		e.setHidden(document.getBoolean("hidden"));
		e.setSystem(document.getBoolean("system"));
		return e;
	}
	
	@Override
	public AbstractObject getById(ObjectTemplate def, String value) throws RepositoryException, ObjectException, ValidationException {
		
		List<SearchField> search = new ArrayList<>();
		search.add(SearchField.eq("uuid", value));
		for(FieldTemplate field : def.getFields()) {
			if(field.isAlternativeId() && NumberUtils.isCreatable(value)) {
				search.add(SearchField.eq(field.getResourceKey(), DocumentHelper.fromString(field, value)));
			}
		}
		Document document = db.get(def.getCollectionKey(),
				getDatabase(def), 
				SearchField.or(search.toArray(new SearchField[0])));
		if(Objects.isNull(document)) {
			throw new ObjectNotFoundException(String.format("No document for resource %s with value %s", def.getResourceKey(), value));
		}
		return buildEntity(def, document);
	}

	@Override
	public void deleteByUUID(ObjectTemplate def, String uuid) throws RepositoryException, ObjectException {
		db.deleteByUUID(uuid, def.getCollectionKey(), getDatabase(def));
	}

	@Override
	public void deleteAll(ObjectTemplate def) throws RepositoryException, ObjectException {
		
		db.dropCollection(def.getCollectionKey(), getDatabase(def));
	}
	
	@Override
	public void deleteByUUIDOrAltId(ObjectTemplate def, String value) throws RepositoryException, ObjectException, ValidationException {
		List<SearchField> search = new ArrayList<>();
		search.add(SearchField.eq("uuid", value));
		for(FieldTemplate field : def.getFields()) {
			if(field.isAlternativeId()) {
				search.add(SearchField.eq(field.getResourceKey(), DocumentHelper.fromString(field, value)));
			}
		}
		db.delete(def.getCollectionKey(), getDatabase(def), 
				SearchField.or(search.toArray(new SearchField[0])));
	}
	
	@Override
	public String save(AbstractObject entity) throws RepositoryException, ObjectException {
		
		ObjectTemplate template = templateRepository.get(SearchField.eq("resourceKey", entity.getResourceKey()));
		
		//validateReferences(template, entity);

		Document document = new Document(entity.getDocument());
		db.insertOrUpdate(document, template.getCollectionKey(), getDatabase(template));
		return document.getString("_id");
	}
	
	@Override
	public void delete(AbstractObject entity) throws RepositoryException, ObjectException {
		
		ObjectTemplate def = templateRepository.get(SearchField.eq("resourceKey", entity.getResourceKey()));
		
		db.delete(def.getCollectionKey(), getDatabase(def), 
				SearchField.eq("_id", entity.getUuid()));

	}

	@Override
	public Collection<AbstractObject> table(ObjectTemplate def, int offset, int limit, String sortColumn, SortOrder order, SearchField... fields) {
		List<AbstractObject> results = new ArrayList<>();
		
		for(Document document : db.searchTable(def.getCollectionKey(), getDatabase(def), offset, limit, order, sortColumn, fields)) {
			results.add(buildEntity(def, document));
		}
		
		return results;
	}
	
	@Override
	public long count(ObjectTemplate def, SearchField... fields) {
		return db.count(def.getCollectionKey(), getDatabase(def), fields);
	}
	
	private String getDatabase(ObjectTemplate def) {
		if(def.getResourceKey().equals(Tenant.RESOURCE_KEY)) {
			return TenantRepository.TENANT_DATABASE;
		}
		if(def.isSystem()) {
			return TenantService.SYSTEM_UUID;
		} else {
			return tenantService.getCurrentTenant().getUuid();
		}
	}

	@Override
	public long count(ObjectTemplate def, String searchField, String searchValue) {
		return db.count(def.getCollectionKey(), searchField, searchValue, getDatabase(def));
	}

	
	class ObjectIterable implements Iterable<AbstractObject> {

		Iterable<Document> iterator;
		ObjectTemplate def;
		public ObjectIterable(ObjectTemplate def, Iterable<Document> iterator) {
			this.def = def;
			this.iterator = iterator;
		}

		@Override
		public Iterator<AbstractObject> iterator() {
			return new ConvertingIterator(iterator.iterator());
		}
	
		class ConvertingIterator implements Iterator<AbstractObject> {

			Iterator<Document> iterator;
			
			public ConvertingIterator(Iterator<Document> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public AbstractObject next() {
				return buildEntity(def, iterator.next());
			}
		}
	}

}
