package com.jadaptive.app.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.app.db.DocumentHelper;

@Service
public class CsvImportServiceImpl implements CsvImportService {

	static Logger log = LoggerFactory.getLogger(CsvImportServiceImpl.class);
	
	@Autowired
	private ObjectService entityService;

	ThreadLocal<Boolean> importing = ThreadLocal.withInitial(() -> Boolean.FALSE);
	
	@Override
	public long importCsv(ObjectTemplate template, InputStream in, boolean containsHeader, String... orderedFields)
			throws IOException {
		return importCsv(template, in, '"', ',', true, 0, false, false, containsHeader, orderedFields);
	}
	
	@Override
	public long importCsv(ObjectTemplate template, InputStream in, char quoteChar, char delimiterChar, boolean ignoreEmptyLines, 
			int maxLinesPerRow, boolean surroundingSpacesNeedQuotes, boolean skipComments, boolean containsHeader, String... orderedFields) 
				throws IOException {

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
		long errors = 0;
		ICsvListReader listReader = null;
		
		if(log.isInfoEnabled()) {
        	log.info("Starting import of entities from InputStream quoteChar={} delimChar={} headers={}", 
        			quoteChar, delimiterChar, containsHeader);
        }
        try {
        	
        		importing.set(Boolean.TRUE);
                listReader = new CsvListReader(new InputStreamReader(in), csvPreferences.build());
                
                if(containsHeader) {
                	listReader.getHeader(true);
                }
                
                List<String> results;
   
                while((results = listReader.read()) != null ) {
                        
                	Map<String,String[]> objectParameters = new HashMap<>();
                	
                    for(int i=0; i < orderedFields.length && i < results.size();i++)  {
                    	String name = orderedFields[i];
                    	if(StringUtils.isBlank(name)) {
                    		continue;
                    	}
                    	
                    	String value = results.get(i);
                    	
                    	objectParameters.put(name, new String[] { value });
                    }

                    try {
	                    entityService.saveOrUpdate(
	                    		DocumentHelper.buildRootObject(Request.get(), objectParameters, 
	                    		template.getResourceKey(), template));
	                    
	                    count++;
                    
                    } catch(Throwable t) {
                    	log.info("Import error", t);
                    	errors++;
                    }
               
                }
                
                if(log.isInfoEnabled()) {
                	log.info("Imported {} entities with {} errors", count, errors);
                }
                
                return count;
                
        } finally {
        	
        	importing.set(Boolean.FALSE);
            if( listReader != null ) {
                    listReader.close();
            }
        }
  
	}

	@Override
	public boolean isImporting() {
		return importing.get();
	}

}
