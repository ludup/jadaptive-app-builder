package com.jadaptive.app.json;

import com.jadaptive.api.json.RequestStatusImpl;

public class UUIDStatus extends RequestStatusImpl {

	String uuid;
	
	public UUIDStatus(String uuid) {
		super(true);
		this.uuid = uuid;
	}
	
	public UUIDStatus(boolean success, String uuid, String message) {
		super(success, message);
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
