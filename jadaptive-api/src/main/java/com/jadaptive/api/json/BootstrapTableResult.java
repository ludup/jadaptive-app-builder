package com.jadaptive.api.json;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BootstrapTableResult<T> {

	private long total;
	private Collection<?> rows;
	private T resource;
//	private ObjectTemplate template; 
	

	public BootstrapTableResult(List<?> rows) {
		this(rows, rows.size());
	}
	
	public BootstrapTableResult(Collection<?> rows, long total/*, ObjectTemplate template*/) {
		this.rows = rows;
		this.total = total;
//		this.template = template;
	}

//	public ObjectTemplate getTemplate() {
//		return template;
//	}
	
	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Collection<?> getRows() {
		return rows;
	}

	public void setRows(Collection<?> rows) {
		this.rows = rows;
	}

	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}


}
