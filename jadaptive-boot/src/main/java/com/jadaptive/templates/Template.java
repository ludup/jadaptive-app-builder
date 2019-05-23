package com.jadaptive.templates;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.jadaptive.Utils;
import com.jadaptive.entity.repository.AbstractUUIDEntityImpl;
import com.jadaptive.entity.repository.DataSourceEntity;

public class Template extends AbstractUUIDEntityImpl implements DataSourceEntity {

	String version; 
	Date timestamp;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version.replace(".json", "");
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void store(Map<String, Map<String,String>> properties) throws ParseException {
		
		Map<String,String> m = new HashMap<>();
		
		super.toMap(m);
	
		m.put("version", version);
		m.put("timestamp", timestamp==null ? Utils.formatDateTime(new Date()) : Utils.formatDateTime(timestamp));
		
		properties.put(getUuid(), m);
	}
	
	public void load(String uuid, Map<String,Map<String,String>> properties) throws ParseException {

		Map<String,String> m = properties.get(uuid);
		
		super.fromMap(m);
		
		this.version = m.get("version");
		this.timestamp = Utils.parseDateTime(m.get("timestamp"));
	}
}
