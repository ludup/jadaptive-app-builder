package com.jadaptive.api.cluster;

import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.template.BroadcastEvent;

public interface BroadcastableEvent {

	public static boolean isBroadcastable(SystemEvent evt) {
		return evt instanceof BroadcastableEvent || evt.getClass().getAnnotation(BroadcastEvent.class) != null;
	}
}
