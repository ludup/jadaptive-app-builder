package com.jadaptive.app.role;

import java.util.Iterator;

import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityToUUIDIterator implements Iterator<String> {

	Iterator<? extends UUIDEntity> it;
	
	public UUIDEntityToUUIDIterator(Iterator<? extends UUIDEntity> it) {
		this.it = it;
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public String next() {
		return it.next().getUuid();
	}

}
