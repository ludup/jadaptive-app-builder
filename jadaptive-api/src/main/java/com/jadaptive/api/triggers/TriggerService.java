package com.jadaptive.api.triggers;

import com.jadaptive.api.events.SystemEvent;

public interface TriggerService {


	void onAuditEvent(SystemEvent evt);

}
