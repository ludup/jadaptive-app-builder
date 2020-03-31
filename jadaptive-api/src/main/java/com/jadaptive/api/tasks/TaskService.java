package com.jadaptive.api.tasks;

public interface TaskService {

	<T extends Task> TaskImpl<T> getTaskImplementation(T task);

}
