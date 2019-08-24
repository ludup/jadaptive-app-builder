package com.jadaaptive.domain;

import java.lang.reflect.Proxy;

public class EntityLocator {

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<?>...classes) {
		return (T) Proxy.newProxyInstance(EntityLocator.class.getClassLoader(), 
				classes, new EntityInvocationHandler());
	}
	
	public static void main(String[] args) {
		Customer customer1 = EntityLocator.get(Customer.class);
		customer1.setName("Project1");
		String name1 = customer1.getName();
		System.out.println(name1);
		customer1.save();
		
		Customer customer2 = EntityLocator.get(Customer.class);
		customer2.setName("Project2");
		String name2 = customer2.getName();
		System.out.println(name2);
		customer2.save();
		
		Customer c3 = ModelHelper.<Customer>find();
	}
	
}
