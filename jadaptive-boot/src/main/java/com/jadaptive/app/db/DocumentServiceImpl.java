package com.jadaptive.app.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
		if(StringUtils.isNotBlank(encodedFile)) {
			return encodedFile.split(";")[0];
		} else {
			return "";
		}
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
	public long getFileTypeContentLength(String encodedFile) {
		String[] vals = encodedFile.split(";");
		if(vals.length > 2) {
			return Long.parseLong(vals[2]);
		}
		return 0L;
	}
	
	@Override
	public InputStream getFileTypeInputStream(String encodedFile) {
		String[] vals = encodedFile.split(";");
		if(vals.length > 3) {
			return new ByteArrayInputStream(Base64.decode(vals[3]));
		}
		return InputStream.nullInputStream();
	}

	@Override
	public String getFileTypeEncodedContent(String encodedFile) {
		String[] vals = encodedFile.split(";");
		if(vals.length > 3) {
			return vals[3];
		}
		return "";
	}
	
	@Override
	public byte[] getFileTypeDecodedContent(String encodedFile) {
		String[] vals = encodedFile.split(";");
		if(vals.length > 3) {
			return Base64.decode(vals[3]);
		}
		return new byte[0];
	}

	@Override
	public String encodeFileTypeValue(String filename, String contentType, long length, byte[] content) {
		try {
			return String.format("%s;%s;%d;%s", filename, contentType, length, new String(Base64.encode(content), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
