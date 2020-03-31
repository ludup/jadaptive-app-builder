package com.jadaptive.app.events;

import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityEvent<T extends UUIDEntity> extends SystemEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	public UUIDEntityEvent(T source) {
		super(source);
	}

}
