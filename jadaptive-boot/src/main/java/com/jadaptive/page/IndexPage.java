package com.jadaptive.page;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jadaptive.datasource.DataSourceEntity;
import com.jadaptive.repository.AbstractUUIDObject;
import com.jadaptive.utils.Utils;

public class IndexPage extends AbstractUUIDObject implements DataSourceEntity {

	List<String> scripts;
	List<String> css;
	String title;
	String body;
	
	
	@Override
	public void store(Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String,String> values = new HashMap<>();
		
		values.put("uuid", getUuid());
		values.put("scripts", Utils.csv(scripts));
		values.put("css", Utils.csv(css));
		values.put("title", title);
		values.put("body", body);
		
		properties.put(getUuid(), values);
		
	}
	
	@Override
	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String,String> values = properties.get(uuid);
		setUuid(uuid);
		title = values.get("title");
		body = values.get("body");
		scripts = Utils.fromCsv(values.get("scripts"));
		css = Utils.fromCsv(values.get("css"));
	}
	
	
}
