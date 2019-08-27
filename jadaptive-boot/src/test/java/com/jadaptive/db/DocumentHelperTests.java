package com.jadaptive.db;

import java.util.Date;

import org.bson.Document;
import org.junit.Test;

public class DocumentHelperTests {
	
	@Test
	public void testObjectSerialization() {
		
		TestObject obj = new TestObject("a", 10L, 100, 1.0F, 2.0D, new Date(), TestEnum.ONE, true);
		Document doc = new Document();
        DocumentHelper.convertObjectToDocument(obj, doc);
        
        
        System.out.println(doc.toString());
	}
	
	
}
