package com.jadaptive.api.json;

import java.util.Collection;

public class TableStatus<T> extends EntityStatus<Collection<T>> {

	long total;
	
	public TableStatus(Collection<T> result, long total) {
		super(result);
		this.total = total;
	}

	public TableStatus(boolean success, String message) {
		super(success, message);
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	
}
