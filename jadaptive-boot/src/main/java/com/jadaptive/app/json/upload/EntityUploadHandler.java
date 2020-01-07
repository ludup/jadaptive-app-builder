package com.jadaptive.app.json.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.jadaptive.api.entity.EntityService;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.upload.UploadHandler;
import com.jadaptive.app.entity.MongoEntity;
import com.jadaptive.utils.FileUtils;

@Extension
public class EntityUploadHandler implements UploadHandler {

	static Logger log = LoggerFactory.getLogger(EntityUploadHandler.class);
	
	@Autowired
	EntityTemplateService templateService; 
	
	@Autowired
	EntityService<MongoEntity> entityService; 
	
	public EntityUploadHandler() {
		System.out.println();
	}
	
	@Override
	public void handleUpload(String handlerName, String uri, Map<String,String> parameters, String filename, InputStream in) throws IOException {
		
		String templateName = FileUtils.firstPathElement(uri);
		EntityTemplate template = templateService.get(templateName);
		
		boolean containsHeader = Boolean.parseBoolean(
				StringUtils.defaultString(parameters.get("containsHeader"), "false"));
		String quoteChar = StringUtils.defaultString(parameters.get("quoteChar"), "\"");
		String delimiterChar = StringUtils.defaultString(parameters.get("delimiterChar"), ",");
		boolean surroundingSpacesNeedQuotes = Boolean.parseBoolean(
				StringUtils.defaultString(parameters.get("surroundingSpacesNeedQuotes"), "false"));
		boolean ignoreEmptyLines = Boolean.parseBoolean(
				StringUtils.defaultString(parameters.get("ignoreEmptyLines"), "true"));
		int maxLinesPerRow = Integer.parseInt(
				StringUtils.defaultString(parameters.get("maxLinesPerRow"), "0"));
		boolean skipComments =  Boolean.parseBoolean(
				StringUtils.defaultString(parameters.get("skipComments"), "false"));
		String orderedFields = parameters.get("orderedFields");
		if(Objects.isNull(orderedFields)) {
			throw new IOException("Missing POST parameter 'orderedFields'");
		}
		
		String[] fields = validateFields(orderedFields.split(","), template);
		
		CsvPreference.Builder csvPreferences = new CsvPreference.Builder(quoteChar.charAt(0), 
				delimiterChar.charAt(0), 
				"\r\n")
				.ignoreEmptyLines(ignoreEmptyLines)
				.maxLinesPerRow(maxLinesPerRow)
				.surroundingSpacesNeedQuotes(surroundingSpacesNeedQuotes);
		
		if(skipComments) {
			csvPreferences.skipComments(new CommentStartsWith("#"));
		}
		
		long count = 0;
		ICsvListReader listReader = null;
        try {
                listReader = new CsvListReader(new InputStreamReader(in), csvPreferences.build());
                
                if(containsHeader) {
                	listReader.getHeader(true);
                }
                
                List<String> results;
                while((results = listReader.read()) != null ) {
                        
                	MongoEntity e = new MongoEntity(templateName);
                    for(int i=0; i < fields.length && i < results.size();i++)  {
                    	String name = fields[i];
                    	if(StringUtils.isBlank(name)) {
                    		continue;
                    	}
                    	if(name.equalsIgnoreCase("UUID")) {
                    		e.setUuid(results.get(i));
                    	} else {
                    		e.setValue(template.getField(name), results.get(i));
                    	}
                    }
                    
                    entityService.saveOrUpdate(e);
                    count++;
                }
                
        }
        finally {
                if( listReader != null ) {
                        listReader.close();
                }
        }
        
        if(log.isInfoEnabled()) {
        	log.info("Uploaded {} entities", count);
        }
	}

	private String[] validateFields(String[] fields, EntityTemplate template) throws IOException {
		for(String field : fields) {
			if(StringUtils.isNotBlank(field) && Objects.isNull(template.getField(field))) {
				if(!field.equalsIgnoreCase("UUID")) {
					throw new IOException(String.format("Invalid field reference '%s'", field));
				}
			}
		}
		return fields;
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