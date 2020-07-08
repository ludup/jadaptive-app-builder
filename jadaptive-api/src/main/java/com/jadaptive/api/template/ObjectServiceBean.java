package com.jadaptive.api.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jadaptive.api.repository.UUIDObjectService;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectServiceBean {
		
		Class<? extends UUIDObjectService<?>> bean();

}
