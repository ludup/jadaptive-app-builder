package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityDeletedEvent<T extends UUIDEntity> extends UUIDEntityEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	public UUIDEntityDeletedEvent(T object) {
		super(generateKey(object.getResourceKey()), object.getResourceKey(), object);
	}

	public static String generateKey(String group) {
		return String.format("%s.deleted", group);
	}
}