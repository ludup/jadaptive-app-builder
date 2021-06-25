package com.jadaptive.utils;

import java.util.Objects;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ValidationException;

public class ObjectUtils {

	@SuppressWarnings("unchecked")
	public static <T extends UUIDEntity> T assertObject(Object object, Class<T> clz) {
		if(Objects.isNull(object)) {
			throw new ValidationException("Object is null");
		}
		if(!clz.isAssignableFrom(object.getClass())) {
			throw new ValidationException(String.format("Cannot cast object to %s", clz.getName()));
		}
		return (T) object;
	}
}
