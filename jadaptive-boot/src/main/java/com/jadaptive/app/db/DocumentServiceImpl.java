package com.jadaptive.app.db;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jadaptive.api.db.DocumentService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationException;

@Service
public class DocumentServiceImpl implements DocumentService {

	@Override
	public AbstractObject buildObject(ObjectTemplate template, Map<String, String[]> parameters) {
		try {
			return DocumentHelper.buildRootObject(parameters , template.getResourceKey(), template);
		} catch (ValidationException | IOException e) {
			throw new ObjectException(e.getMessage(), e);
		}
	}

}
