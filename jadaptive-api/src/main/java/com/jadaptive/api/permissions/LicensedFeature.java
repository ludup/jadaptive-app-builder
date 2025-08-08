package com.jadaptive.api.permissions;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jadaptive.api.product.ProductService.ProductId;

@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(value = LicensedFeatures.class)
public @interface LicensedFeature {

	String value();
	
	FeatureGroup group();
	
	boolean includedWithPAYG() default false;
	
	ProductId[] products() default {};
}
