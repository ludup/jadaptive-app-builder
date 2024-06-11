package com.jadaptive.app.json.upload;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.csv.CsvImportService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.upload.Upload;
import com.jadaptive.api.upload.UploadHandler;
import com.jadaptive.api.upload.UploadIterator;
import com.jadaptive.utils.FileUtils;
import com.jadaptive.utils.ParameterHelper;

@Extension
@Component
public class EntityUploadHandler implements UploadHandler {

	static Logger log = LoggerFactory.getLogger(EntityUploadHandler.class);
	
	@Autowired
	private TemplateService templateService; 

	@Autowired
	private CsvImportService importService; 
	
	public EntityUploadHandler() {
	}

	@Override
	public void handleUpload(String handlerName, String uri, Map<String,String[]> parameters, UploadIterator uploads) throws IOException, SessionTimeoutException, UnauthorizedException {
		
		String templateName = FileUtils.firstPathElement(uri);
		ObjectTemplate template = templateService.get(templateName);
		
		boolean containsHeader = Boolean.parseBoolean(
				StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"containsHeader"), "false"));
		String quoteChar = StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"quoteChar"), "\"");
		String delimiterChar = StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"delimiterChar"), ",");
		boolean surroundingSpacesNeedQuotes = Boolean.parseBoolean(
				StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"surroundingSpacesNeedQuotes"), "false"));
		boolean ignoreEmptyLines = Boolean.parseBoolean(
				StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"ignoreEmptyLines"), "true"));
		int maxLinesPerRow = Integer.parseInt(
				StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"maxLinesPerRow"), "0"));
		boolean skipComments =  Boolean.parseBoolean(
				StringUtils.defaultIfBlank(ParameterHelper.getValue(parameters,"skipComments"), "false"));
		
		String orderedFields = ParameterHelper.getValue(parameters,"orderedFields");
		
		if(Objects.isNull(orderedFields)) {
			throw new IOException("Missing POST parameter 'orderedFields'");
		}
		
		while(uploads.hasNext()) {
			Upload upload = uploads.next();
			importService.importCsv(template, upload.openStream(), quoteChar.charAt(0), delimiterChar.charAt(0), ignoreEmptyLines, maxLinesPerRow, 
				surroundingSpacesNeedQuotes, skipComments, containsHeader, orderedFields.split(","));
		}
	}

	@Override
	public boolean isSessionRequired() {
		/**
		 * Read/write permission required on entity.
		 */
		return true;
	}

	@Override
	public String getURIName() {
		return "entity";
	}

}