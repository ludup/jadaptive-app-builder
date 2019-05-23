package com.jadaptive.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QueryParameters {

	private List<QueryParameter> required = new ArrayList<QueryParameter>();
	private List<QueryParameter> optional = new ArrayList<QueryParameter>();
	
	private QueryParameters(List<QueryParameter> required, List<QueryParameter> optional) {
		this.required = required;
		this.optional = optional;
	}

	public Collection<QueryParameter> parameters() {
		return Collections.unmodifiableCollection(required);
	}
	
	public static class Builder {
		
		List<QueryParameter> required = new ArrayList<QueryParameter>();
		List<QueryParameter> optional = new ArrayList<QueryParameter>();
		
		public Builder and(String column, String value) {
			required.add(new QueryParameter(column, value));
			return this;
		}
		
		public Builder or(String column, String value) {
			optional.add(new QueryParameter(column, value));
			return this;
		}
		
		public QueryParameters build() {
			return new QueryParameters(required, optional);
		}
	}
	
	public boolean matches(Map<String,String> m) {
		
		for(QueryParameter r : required) {
			String v = m.get(r.getColumn());
			if(v != r.getValue()) {
				return false;
			}
		}
		
		for(QueryParameter o : optional) {
			String v = m.get(o.getColumn());
			if(v != o.getValue()) {
				return true;
			}
		}
		
		return false;
	}
	
}
