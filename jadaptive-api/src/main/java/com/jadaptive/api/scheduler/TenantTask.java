package com.jadaptive.api.scheduler;

import org.pf4j.ExtensionPoint;

public interface TenantTask extends Runnable, ExtensionPoint {

	TaskScope getScope();
}
