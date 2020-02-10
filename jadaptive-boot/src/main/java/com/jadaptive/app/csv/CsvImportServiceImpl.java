package com.jadaptive.app.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.jadaptive.api.csv.CsvImportService;
import com.jadaptive.api.entity.EntityService;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.app.entity.MongoEntity;

@Service
public class CsvImportServiceImpl implements CsvImportService {

	static Logger log = LoggerFactory.getLogger(CsvImportServiceImpl.class);
	
	@Autowired
	private EntityService<MongoEntity> entityService;
	
	@Override
	public long importCsv(EntityTemplate template, InputStream in, boolean containsHeader, String... orderedFields)
			throws IOException {
		return importCsv(template, in, '"', ',', true, 0, false, false, containsHeader, orderedFields);
	}
	
	@Override
	public long importCsv(EntityTemplate template, InputStream in, char quoteChar, char delimiterChar, boolean ignoreEmptyLines, 
			int maxLinesPerRow, boolean surroundingSpacesNeedQuotes, boolean skipComments, boolean containsHeader, String... orderedFields) 
				throws IOException {
		
		String[] fields = validateFields(orderedFields, template);
		
		CsvPreference.Builder csvPreferences = new CsvPreference.Builder(quoteChar, 
				delimiterChar, 
				"\r\n")
				.ignoreEmptyLines(ignoreEmptyLines)
				.maxLinesPerRow(maxLinesPerRow)
				.surroundingSpacesNeedQuotes(surroundingSpacesNeedQuotes);
		
		if(skipComments) {
			csvPreferences.skipComments(new CommentStartsWith("#"));
		}
		
		long count = 0;
		ICsvListReader listReader = null;
		
		if(log.isInfoEnabled()) {
        	log.info("Starting import of entities from InputStream quoteChar={} delimChar={} headers={}", 
        			quoteChar, delimiterChar, containsHeader);
        }
        try {
                listReader = new CsvListReader(new InputStreamReader(in), csvPreferences.build());
                
                if(containsHeader) {
                	listReader.getHeader(true);
                }
                
                List<String> results;
                List<String> values = new ArrayList<>();
                while((results = listReader.read()) != null ) {
                        
                	MongoEntity e = new MongoEntity(template.getResourceKey());
                	values.clear();
                    for(int i=0; i < fields.length && i < results.size();i++)  {
                    	String name = fields[i];
                    	if(StringUtils.isBlank(name)) {
                    		continue;
                    	}
                    	
                    	String value = results.get(i);
                    	if(name.equalsIgnoreCase("UUID")) {
                    		e.setUuid(value);
                    	} else {
                    		e.setValue(template.getField(name), value);
                    	}
                    	
                    	values.add(value);
                    }
                    
                    
                    entityService.saveOrUpdate(e);
                    count++;
               
                }
                
                if(log.isInfoEnabled()) {
                	log.info("Imported {} entities", count);
                }
                
                return count;
                
        } finally {
            if( listReader != null ) {
                    listReader.close();
            }
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
}
