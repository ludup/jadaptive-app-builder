package com.jadaptive.api.events;

public class Events {

	enum Type {
		CREATED,
		UPDATED,
		DELETED,
		OTHER
	}
	public static String created(String resourceKey) {
		return String.format("%s.created", resourceKey);
	}
	
	public static String updated(String resourceKey) {
		return String.format("%s.updated", resourceKey);
	}
	
	public static String deleted(String resourceKey) {
		return String.format("%s.deleted", resourceKey);
	}
	
	public Type getEventType(SystemEvent event) {
		if(event.getResourceKey().endsWith(".created")) {
			return Type.CREATED;
		} else if(event.getResourceKey().endsWith(".updated")) {
			return Type.UPDATED;
		} else if(event.getResourceKey().endsWith(".deleted")) {
			return Type.DELETED;
		}
		return Type.OTHER;
	}
}
