package com.jadaptive.passwords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.entity.Entity;
import com.jadaptive.entity.EntityService;
import com.jadaptive.entity.template.EntityTemplate;
import com.jadaptive.entity.template.EntityTemplateService;
import com.jadaptive.entity.template.FieldTemplate;
import com.jadaptive.json.upload.UploadHandler;

@Component
public class BannedPasswordUploadHandler implements UploadHandler {

	static Logger log = LoggerFactory.getLogger(BannedPasswordUploadHandler.class);
	
	
	@Autowired
	EntityService entityService; 
	
	@Autowired
	EntityTemplateService templateService; 
	
	
	@Override
	public void handleUpload(String handlerName, String uri, String filename, InputStream in) throws IOException {
		
		EntityTemplate template = templateService.get("bannedPasswords");
		FieldTemplate t = template.getField("password");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String password;
		int i = 0;
		while((password = reader.readLine()) != null) {
			Entity e = new Entity("bannedPasswords", new Document());
			e.setValue(t, password);
			entityService.saveOrUpdate(e);
			++i;
			if(i % 1000 == 0) {
				log.info("Processed {} passwords", i);
			}
		}

	}

	@Override
	public boolean isSessionRequired() {
		return true;
	}

}
