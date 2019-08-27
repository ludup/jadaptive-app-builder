package com.jadaaptive.domain;

import javassist.Modifier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Unloaded;

public class PersonInterfaceDefinition {
	
	public static Unloaded<?> make() {
		return new ByteBuddy()
				.makeInterface()
		  .name("com.jadaptive.domain.Product")
		  .defineMethod("setName", Void.class, Modifier.PUBLIC).withParameter(String.class)
		  .withoutCode()
		  .defineMethod("getName", String.class, Modifier.PUBLIC)
		  .withoutCode()
		  .make();
	}
}
