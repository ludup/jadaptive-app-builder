package com.jadaptive.plugins.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TenantAwareObjectDatabase;

@Service
public class HTMLTemplateServiceImpl implements HTMLTemplateService {

	static Logger log = LoggerFactory.getLogger(HTMLTemplateServiceImpl.class);
	
	@Autowired
	private TenantAwareObjectDatabase<HTMLTemplate> templateDatbase;
	
	@Override
	public HTMLTemplate getObjectByUUID(String uuid) {
		return templateDatbase.get(uuid, HTMLTemplate.class);
	}

	@Override
	public String saveOrUpdate(HTMLTemplate object) {
		templateDatbase.saveOrUpdate(object);
		
//		if(Boolean.getBoolean("jadaptive.development")) {
//			log.info("Template {} has content hash {}", object.getName(), DigestUtils.sha256Hex(object.getHtml() + "|" ));
//		}
		
		return object.getUuid();
	}

	@Override
	public void deleteObject(HTMLTemplate object) {
		templateDatbase.delete(object);
	}

	@Override
	public void deleteObjectByUUID(String uuid) {
		templateDatbase.delete(uuid, HTMLTemplate.class);
	}

	@Override
	public Iterable<HTMLTemplate> allObjects() {
		return templateDatbase.list(HTMLTemplate.class);
	}
	
	
}
