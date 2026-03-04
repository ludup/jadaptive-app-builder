package com.jadaptive.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.NamePairValue;

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

	public static Collection<NamePairValue> toNamePairValueCollection(Iterable<AbstractObject> objectCollection) {
		List<NamePairValue> results = new ArrayList<>();
		objectCollection.forEach((object) -> {
			results.add(new NamePairValue(object.getUuid(), (String) object.getValue("name")));
		});
		return results;
	}
}
