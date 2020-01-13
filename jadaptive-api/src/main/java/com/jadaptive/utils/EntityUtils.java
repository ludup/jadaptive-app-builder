package com.jadaptive.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class EntityUtils {

	
	
	public static Collection<String> getUUIDs(Collection<? extends AbstractUUIDEntity> entities) {
		List<String> tmp = new ArrayList<>();
		for(AbstractUUIDEntity e : entities) {
			tmp.add(e.getUuid());
		}
		return tmp;
	}
}
