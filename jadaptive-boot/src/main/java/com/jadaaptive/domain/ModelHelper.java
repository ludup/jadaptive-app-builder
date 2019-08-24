package com.jadaaptive.domain;

public class ModelHelper<T> {

	public static <T> T find() {
		return null;
	}
	
	
	public static class Where<A> {
		public void eq(String key, A value) {
			
		}
		
		static {
			Where<String> where = new Where<String>();
			where.eq("name", "strange");
		}
	}
}
