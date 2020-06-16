package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.Collection;

public class UUIDList extends ArrayList<String> {

	private static final long serialVersionUID = -4723372950320588321L;

	public UUIDList() {
		super();
	}

	public UUIDList(Collection<? extends String> c) {
		super(c);
	}

	public UUIDList(int initialCapacity) {
		super(initialCapacity);
	}

	
}
