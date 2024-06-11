package com.jadaptive.api.csv;

import java.io.IOException;
import java.io.InputStream;

import com.jadaptive.api.template.ObjectTemplate;

public interface CsvImportService {

	long importCsv(ObjectTemplate template, InputStream in, char quoteChar, char delimiterChar,
			boolean ignoreEmptyLines, int maxLinesPerRow, boolean surroundingSpacesNeedQuotes, boolean skipComments,
			boolean containsHeader, String... orderedFields) throws IOException;
	
	long importCsv(ObjectTemplate template, InputStream in, 
			boolean containsHeader, String... orderedFields) throws IOException;

	boolean isImporting();

}
