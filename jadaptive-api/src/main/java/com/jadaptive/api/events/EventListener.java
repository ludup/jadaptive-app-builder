package com.jadaptive.api.events;

public interface EventListener<T> {

	void onEvent(T evt);
}
