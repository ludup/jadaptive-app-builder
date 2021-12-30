package com.jadaptive.api.events;

public class Events {

	public static String created(String resourceKey) {
		return String.format("%s.created", resourceKey);
	}
	
	public static String updated(String resourceKey) {
		return String.format("%s.updated", resourceKey);
	}
	
	public static String deleted(String resourceKey) {
		return String.format("%s.deleted", resourceKey);
	}
}
