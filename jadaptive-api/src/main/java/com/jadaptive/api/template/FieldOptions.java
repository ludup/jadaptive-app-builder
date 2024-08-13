package com.jadaptive.api.template;

public enum FieldOptions {

	/**
	 * If this is a searchable field then the user MUST have
	 * the required permissions on the object reference type
	 * in order to search by it. This should be used to prevent
	 * exposure of objects through the search mechanism.
	 */
	SEARCH_REQUIRE_REFERENCE_READ,
	
	/**
	 * When added to a reference field on an Object. When that 
	 * object is deleted it will cascade the delete down to the 
	 * object referenced by the field. i.e. the referenced object
	 * will be deleted in addition to the declaring object.
	 */
	CASCADE_DELETE,
	
	/**
	 * When added to a reference field on an Object. When the referenced
	 * object is deleted, it will cascade the delete down to the object
	 * that declared this in its field. i.e.  the declaring object will be
	 * deleted.
	 */
	CASCADE_ON_DELETED_REFERENCE
}
