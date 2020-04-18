package com.jadaptive.api.tasks;

import java.util.Collection;

public interface TaskService {

	<T extends Task> TaskImpl<T> getTaskImplementation(T task);

	Collection<String> getTaskResourceKeys();

}
