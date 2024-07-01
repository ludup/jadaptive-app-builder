package com.jadaptive.app.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;
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
	
	@Override
	public String getFileTypeFilename(String encodedFile) {
		return encodedFile.split(";")[0];
	}
	
	@Override
	public String getFileTypeContentType(String encodedFile) {
		String[] vals = encodedFile.split(";");
		if(vals.length > 1) {
			return vals[1];
		}
		return "";
	}
	
	@Override
	public InputStream getFileTypeInputStream(String encodedFile) {
		String[] vals = encodedFile.split(";");
		if(vals.length > 2) {
			return new ByteArrayInputStream(Base64.decode(vals[2]));
		}
		return InputStream.nullInputStream();
	}

}
