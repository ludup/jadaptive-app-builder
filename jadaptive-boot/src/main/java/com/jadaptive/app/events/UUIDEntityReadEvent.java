package com.jadaptive.app.events;

import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityReadEvent<T extends UUIDEntity> extends SystemEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	public UUIDEntityReadEvent(T source) {
		super(source, String.format("%s.read", source.getResourceKey()));
	}

}
