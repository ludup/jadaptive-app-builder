package com.jadaptive.api.events;

public class Events {

	public static String creating(String resourceKey) {
		return String.format("%s.creating", resourceKey);
	}
	
	public static String created(String resourceKey) {
		return String.format("%s.created", resourceKey);
	}
	
	public static String updating(String resourceKey) {
		return String.format("%s.updating", resourceKey);
	}
	
	public static String updated(String resourceKey) {
		return String.format("%s.updated", resourceKey);
	}

	public static String deleting(String resourceKey) {
		return String.format("%s.deleting", resourceKey);
	}
	
	public static String deleted(String resourceKey) {
		return String.format("%s.deleted", resourceKey);
	}
	
	public static String assigned(String resourceKey) {
		return String.format("%s.assigned", resourceKey);
	}
	
	public static String unassigned(String resourceKey) {
		return String.format("%s.unassigned", resourceKey);
	}
}
