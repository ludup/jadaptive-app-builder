package com.jadaptive.api.template;

import org.jsoup.nodes.Element;

import com.jadaptive.api.entity.AbstractObject;

public interface DynamicColumnService {

	Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate);

	
}
