package com.jadaptive.api.db;

import java.io.InputStream;
import java.util.Map;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.ObjectTemplate;

public interface DocumentService {

	AbstractObject buildObject(ObjectTemplate template, Map<String, String[]> parameters);

	String getFileTypeFilename(String encodedFile);

	String getFileTypeContentType(String encodedFile);

	InputStream getFileTypeInputStream(String encodedFile);

}
