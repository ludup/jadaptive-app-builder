package com.jadaptive.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ValidationException;

public class ObjectUtils {

	@SuppressWarnings("unchecked")
	public static <T> T assertObject(Object object, Class<T> clz) {
		if(Objects.isNull(object)) {
			throw new ValidationException("Object is null");
		}
		if(!clz.isAssignableFrom(object.getClass())) {
			throw new ValidationException(String.format("Cannot cast object to %s", clz.getName()));
		}
		return (T) object;
	}

	public static Collection<String> entityToUUIDCollection(Collection<? extends UUIDEntity> values) {
		var results = new ArrayList<String>();
		values.forEach((value)-> {
			results.add(value.getUuid());
		});
		return results;
	}
}
