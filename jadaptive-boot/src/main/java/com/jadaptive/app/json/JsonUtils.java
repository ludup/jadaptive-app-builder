package com.jadaptive.app.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	public static boolean isValidJSON(final String json) {
		   try {
		      final JsonParser parser = new ObjectMapper().getFactory()
		            .createParser(json);
		      while (parser.nextToken() != null) {
		      }
		      return true;
		   } catch (JsonParseException jpe) {
		      return false;
		   } catch (IOException ioe) {
			   return false;
		   }

		}
	
	public static String prettyPrintJson(String output) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(output, Object.class));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);

		}
	}
}
