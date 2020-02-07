package com.jadaptive.api.app;

import java.util.Collection;

public interface AnnotationService {

	<T> Collection<Class<T>> getClassesAnnotatedWith(Class<T> type);

}
