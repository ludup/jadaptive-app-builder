package com.jadaaptive.domain;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class EntityInvocationHandler implements InvocationHandler {

	private final RawValueWrapper rawValueWrapper = new RawValueWrapper();
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		String key = methodName.replaceAll("set", "").replaceAll("get", "");
		if (methodName.startsWith("get")) {
			return rawValueWrapper.getRaw().get(key);
		} else if (methodName.startsWith("set")) {
			rawValueWrapper.getRaw().put(key, args[0]);
		} else if ("save".equals(methodName)) {
			System.out.println("Saved into database " + UUID.randomUUID().toString());
		} else if ("hashCode".equals(methodName)) {
			return rawValueWrapper.hashCode();
		} else if ("toString".equals(methodName)) {
			return rawValueWrapper.getRaw().toString();
		} 
		return null;
	}

}
