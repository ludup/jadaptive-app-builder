package com.jadaptive.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.app.db.DocumentHelper;
import com.mongodb.BasicDBList;

public class DocumentHelperTests {
	
	
	@Test
	public void testObjectCollectionSerializationAndDeserialization() throws ParseException {
		
		TestObjectCollections obj = new TestObjectCollections(new TestSimpleObject("one"),
				new TestSimpleObject("two"), new TestSimpleObject("three"));
		
		Document doc = new Document();
        DocumentHelper.convertObjectToDocument(obj, doc);
        
        TestObjectCollections des = DocumentHelper.convertDocumentToObject(TestObjectCollections.class, doc);
        
        List<TestSimpleObject> values = des.getValues();
        
        assertNotNull(values);
        assertEquals(3, values.size());
        TestSimpleObject one = values.get(0);
        assertEquals("one", one.getName());
        
        TestSimpleObject two = values.get(1);
        assertEquals("two", two.getName());
        
        TestSimpleObject three = values.get(2);
        assertEquals("three", three.getName());
        
        assertKnownFields(obj, des);

	}
	
	private void assertKnownFields(AbstractUUIDEntity obj, AbstractUUIDEntity des) {
		
		//assertEquals(obj.isHidden(), des.isHidden());
		assertEquals(obj.isSystem(), des.isSystem());
		assertEquals(obj.getUuid(), des.getUuid());
		
	}

	@Test
	public void testObjectCollectionDeserialization() throws ParseException {
		
		Document doc = new Document();
		
		Document one = new Document();
		one.put("name", "one");
		one.put("_clz", TestSimpleObject.class.getName());
		Document two = new Document();
		two.put("name", "two");
		two.put("_clz", TestSimpleObject.class.getName());
		Document three = new Document();
		three.put("name", "three");
		three.put("_clz", TestSimpleObject.class.getName());
		BasicDBList list = new BasicDBList();
		list.add(one);
		list.add(two);
		list.add(three);
		
		doc.put("values", list);
		doc.put("_clz", TestObjectCollections.class.getName());
		
		System.out.println(doc.toString());
		
		TestObjectCollections obj = DocumentHelper.convertDocumentToObject(TestObjectCollections.class, doc);

		assertNotNull(obj.getValues());
        assertEquals(3, obj.getValues().size());
        
        assertEquals("one", obj.getValues().get(0).getName());
        assertEquals("two", obj.getValues().get(1).getName());
        assertEquals("three", obj.getValues().get(2).getName());
        
        
	}
	
	@Test
	public void testEnumCollectionSerialization() {
		
		TestEnumCollections obj = new TestEnumCollections(TestEnum.ONE, TestEnum.TWO, TestEnum.THREE);
		Document doc = new Document();
        DocumentHelper.convertObjectToDocument(obj, doc);
        
        System.out.println(doc);
        
        List<?> values = (List<?>) doc.get("values");
        
        assertNotNull(values);
        assertTrue(values.contains("ONE"));
        assertTrue(values.contains("TWO"));
        assertTrue(values.contains("THREE"));
        assertFalse(values.contains("FOUR"));
	}
	
	@Test
	public void testEnumCollectionDeserialization() throws ParseException {
		
		Document doc = new Document();
		List<String> strings = Arrays.asList("ONE", "TWO", "THREE");
		doc.put("values", strings);
		doc.put("_clz", TestEnumCollections.class.getName());
		TestEnumCollections obj = DocumentHelper.convertDocumentToObject(TestEnumCollections.class, doc);

        assertNotNull(obj.getValues());
        assertTrue(obj.getValues().contains(TestEnum.ONE));
        assertTrue(obj.getValues().contains(TestEnum.TWO));
        assertTrue(obj.getValues().contains(TestEnum.THREE));
        assertFalse(obj.getValues().contains(TestEnum.FOUR));
	}
	
	@Test
	public void testStringCollectionSerialization() {
		
		TestStringCollections obj = new TestStringCollections("one", "two", "three");
		Document doc = new Document();
        DocumentHelper.convertObjectToDocument(obj, doc);
        
        System.out.println(doc);
        
        List<?> values = (List<?>) doc.get("strings");
        
        assertNotNull(values);
        assertTrue(values.contains("one"));
        assertTrue(values.contains("two"));
        assertTrue(values.contains("three"));
        assertFalse(values.contains("four"));
	}
	
	@Test
	public void testStringCollectionDeserialization() throws ParseException {
		
		Document doc = new Document();
		List<String> strings = Arrays.asList("one", "two", "three");
		doc.put("strings", strings);
		doc.put("_clz", TestStringCollections.class.getName());
		TestStringCollections obj = DocumentHelper.convertDocumentToObject(TestStringCollections.class, doc);

        assertNotNull(obj.getStrings());
        assertTrue(obj.getStrings().contains("one"));
        assertTrue(obj.getStrings().contains("two"));
        assertTrue(obj.getStrings().contains("three"));
        assertFalse(obj.getStrings().contains("four"));
	}
	
	@Test
	public void testEmptyStringCollectionSerialization() {
		
		TestStringCollections obj = new TestStringCollections();
		Document doc = new Document();
        DocumentHelper.convertObjectToDocument(obj, doc);
        
        System.out.println(doc);
        
        List<?> values = (List<?>) doc.get("strings");
        
        assertNull(values);

	}
	
	@Test
	public void testEmptyStringCollectionDeserialization() throws ParseException {
		
		
		Document doc = new Document();
		doc.put("_clz", TestStringCollections.class.getName());
		TestStringCollections obj = DocumentHelper.convertDocumentToObject(TestStringCollections.class, doc);

        assertNull(obj.getStrings());
	}
	
	@Test
	public void testObjectSerialization() {
		
		Date date = new Date();
		EmbeddedObject embedded = new EmbeddedObject("test", Long.MAX_VALUE, Integer.MIN_VALUE, Float.MAX_VALUE, Double.MIN_VALUE, date, TestEnum.FIVE, false);
		TestFieldTypesObject obj = new TestFieldTypesObject("a", 10L, 100, 1.0F, 2.0D, date, TestEnum.THREE, true, embedded);
		Document doc = new Document();
        DocumentHelper.convertObjectToDocument(obj, doc);
        
        System.out.println(doc);
        
        assertEquals("a", doc.get("string"));
        assertEquals(10L, doc.get("longNumber"));
        assertEquals(100, doc.get("intNumber"));
        assertEquals(1.0F, doc.get("floatNumber"));
        assertEquals(2.0D, doc.get("doubleNumber"));
        assertEquals(date, doc.get("date"));
        assertEquals("THREE", doc.get("enumField"));
        assertEquals(true, doc.get("bool"));
        assertEquals(false, doc.get("system"));
        assertEquals(false, doc.get("hidden"));
        
        @SuppressWarnings("unchecked")
		Map<String,Object> embeddedDocument = (Map<String,Object>) doc.get("embedded");
        assertNotNull(embeddedDocument);
        assertEquals("test", embeddedDocument.get("embeddedString"));
        assertEquals(Long.MAX_VALUE, embeddedDocument.get("embeddedLong"));
        assertEquals(Integer.MIN_VALUE, embeddedDocument.get("embeddedInt"));
        assertEquals(Float.MAX_VALUE, embeddedDocument.get("embeddedFloat"));
        assertEquals(Double.MIN_VALUE, embeddedDocument.get("embeddedDouble"));
        assertEquals(date, embeddedDocument.get("embeddedDate"));
        assertEquals("FIVE", embeddedDocument.get("embeddedEnum"));
        assertEquals(false, embeddedDocument.get("embeddedBool"));
        assertEquals(false, embeddedDocument.get("system"));
        assertEquals(false, embeddedDocument.get("hidden"));
        
	}
	
	@Test
	public void testObjectDeserialization() throws ParseException {
		
		Date date = new Date();
		
		Document doc = new Document();
		doc.put("_clz", TestFieldTypesObject.class.getName());
        doc.put("string", "a");
        doc.put("longNumber", 10L);
        doc.put("intNumber", 100);
        doc.put("floatNumber", 1.0F);
        doc.put("doubleNumber", 2.0D);
        doc.put("date", date);
        doc.put("enumField", "THREE");
        doc.put("bool", true);
        
        Document embeddedDoc = new Document();
        embeddedDoc.put("_clz", EmbeddedObject.class.getName());
        embeddedDoc.put("embeddedString", "test");
        embeddedDoc.put("embeddedLong", Long.MAX_VALUE);
        embeddedDoc.put("embeddedInt", Integer.MIN_VALUE);
        embeddedDoc.put("embeddedFloat", Float.MAX_VALUE);
        embeddedDoc.put("embeddedDouble", Double.MIN_VALUE);
        embeddedDoc.put("embeddedDate", date);
        embeddedDoc.put("embeddedEnum", "FIVE");
        embeddedDoc.put("embeddedBool", false);

        doc.put("embedded", embeddedDoc);
        
        TestFieldTypesObject obj = DocumentHelper.convertDocumentToObject(TestFieldTypesObject.class, doc);
        
        assertEquals("a", obj.getString());
        assertEquals(Long.valueOf(10L), obj.getLongNumber());
        assertEquals(Integer.valueOf(100), obj.getIntNumber());
        assertEquals(Float.valueOf(1.0F), obj.getFloatNumber());
        assertEquals(Double.valueOf(2.0D), obj.getDoubleNumber());
        assertEquals(date, obj.getDate());
        assertEquals(TestEnum.THREE, obj.getEnumField());
        assertEquals(Boolean.TRUE, obj.getBool());
        //assertEquals(Boolean.FALSE, obj.isHidden());
        assertEquals(Boolean.FALSE, obj.isSystem());
        
        assertNotNull(obj.getEmbedded());
        
        EmbeddedObject e = obj.getEmbedded();
        assertEquals("test", e.getEmbeddedString());
        assertEquals(Long.valueOf(Long.MAX_VALUE), e.getEmbeddedLong());
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), e.getEmbeddedInt());
        assertEquals(Float.valueOf(Float.MAX_VALUE), e.getEmbeddedFloat());
        assertEquals(Double.valueOf(Double.MIN_VALUE), e.getEmbeddedDouble());
        assertEquals(date, e.getEmbeddedDate());
        assertEquals(TestEnum.FIVE, e.getEmbeddedEnum());
        assertEquals(Boolean.FALSE, e.getEmbeddedBool());
        //assertEquals(Boolean.FALSE, e.isHidden());
        assertEquals(Boolean.FALSE, e.isSystem());
	}
	
	
}
