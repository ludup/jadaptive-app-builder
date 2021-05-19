package com.jadaptive.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.app.db.DocumentHelper;
import com.mongodb.BasicDBList;

public class DocumentHelperTests {
	
	@Autowired
	private DocumentHelper documentHelper;
	
//	@Test
//	public void testObjectCollectionSerializationAndDeserialization() throws ParseException {
//		
//		TestObjectCollections obj = new TestObjectCollections(new TestSimpleObject("one"),
//				new TestSimpleObject("two"), new TestSimpleObject("three"));
//		
//		Document doc = new Document();
//        DocumentHelper.convertObjectToDocument(obj, doc);
//        
//        TestObjectCollections des = documentHelper.convertDocumentToObject(TestObjectCollections.class, doc);
//        
//        List<TestSimpleObject> values = des.getValues();
//        
//        Assert.assertNotNull(values);
//        Assert.assertEquals(3, values.size());
//        TestSimpleObject one = values.get(0);
//        Assert.assertEquals("one", one.getName());
//        
//        TestSimpleObject two = values.get(1);
//        Assert.assertEquals("two", two.getName());
//        
//        TestSimpleObject three = values.get(2);
//        Assert.assertEquals("three", three.getName());
//        
//        assertKnownFields(obj, des);
//
//	}
//	
//	private void assertKnownFields(AbstractUUIDEntity obj, AbstractUUIDEntity des) {
//		
//		assertEquals(obj.isHidden(), des.isHidden());
//		assertEquals(obj.isSystem(), des.isSystem());
//		assertEquals(obj.getUuid(), des.getUuid());
//		
//	}
//
//	@Test
//	public void testObjectCollectionDeserialization() throws ParseException {
//		
//		Document doc = new Document();
//		
//		Document one = new Document();
//		one.put("name", "one");
//		one.put("_clz", TestSimpleObject.class.getName());
//		Document two = new Document();
//		two.put("name", "two");
//		two.put("_clz", TestSimpleObject.class.getName());
//		Document three = new Document();
//		three.put("name", "three");
//		three.put("_clz", TestSimpleObject.class.getName());
//		BasicDBList list = new BasicDBList();
//		list.add(one);
//		list.add(two);
//		list.add(three);
//		
//		doc.put("values", list);
//		doc.put("_clz", TestObjectCollections.class.getName());
//		
//		System.out.println(doc.toString());
//		
//		TestObjectCollections obj = DocumentHelper.convertDocumentToObject(TestObjectCollections.class, doc);
//
//        Assert.assertNotNull(obj.getValues());
//        Assert.assertEquals(3, obj.getValues().size());
//        
//        Assert.assertEquals("one", obj.getValues().get(0).getName());
//        Assert.assertEquals("two", obj.getValues().get(1).getName());
//        Assert.assertEquals("three", obj.getValues().get(2).getName());
//        
//        
//	}
//	
//	@Test
//	public void testEnumCollectionSerialization() {
//		
//		TestEnumCollections obj = new TestEnumCollections(TestEnum.ONE, TestEnum.TWO, TestEnum.THREE);
//		Document doc = new Document();
//        DocumentHelper.convertObjectToDocument(obj, doc);
//        
//        System.out.println(doc);
//        
//        List<?> values = (List<?>) doc.get("values");
//        
//        Assert.assertNotNull(values);
//        Assert.assertTrue(values.contains("ONE"));
//        Assert.assertTrue(values.contains("TWO"));
//        Assert.assertTrue(values.contains("THREE"));
//        Assert.assertFalse(values.contains("FOUR"));
//	}
//	
//	@Test
//	public void testEnumCollectionDeserialization() throws ParseException {
//		
//		Document doc = new Document();
//		List<String> strings = Arrays.asList("ONE", "TWO", "THREE");
//		doc.put("values", strings);
//		doc.put("_clz", TestEnumCollections.class.getName());
//		TestEnumCollections obj = DocumentHelper.convertDocumentToObject(TestEnumCollections.class, doc);
//
//        Assert.assertNotNull(obj.getValues());
//        Assert.assertTrue(obj.getValues().contains(TestEnum.ONE));
//        Assert.assertTrue(obj.getValues().contains(TestEnum.TWO));
//        Assert.assertTrue(obj.getValues().contains(TestEnum.THREE));
//        Assert.assertFalse(obj.getValues().contains(TestEnum.FOUR));
//	}
//	
//	@Test
//	public void testStringCollectionSerialization() {
//		
//		TestStringCollections obj = new TestStringCollections("one", "two", "three");
//		Document doc = new Document();
//        DocumentHelper.convertObjectToDocument(obj, doc);
//        
//        System.out.println(doc);
//        
//        List<?> values = (List<?>) doc.get("strings");
//        
//        Assert.assertNotNull(values);
//        Assert.assertTrue(values.contains("one"));
//        Assert.assertTrue(values.contains("two"));
//        Assert.assertTrue(values.contains("three"));
//        Assert.assertFalse(values.contains("four"));
//	}
//	
//	@Test
//	public void testStringCollectionDeserialization() throws ParseException {
//		
//		Document doc = new Document();
//		List<String> strings = Arrays.asList("one", "two", "three");
//		doc.put("strings", strings);
//		doc.put("_clz", TestStringCollections.class.getName());
//		TestStringCollections obj = DocumentHelper.convertDocumentToObject(TestStringCollections.class, doc);
//
//        Assert.assertNotNull(obj.getStrings());
//        Assert.assertTrue(obj.getStrings().contains("one"));
//        Assert.assertTrue(obj.getStrings().contains("two"));
//        Assert.assertTrue(obj.getStrings().contains("three"));
//        Assert.assertFalse(obj.getStrings().contains("four"));
//	}
//	
//	@Test
//	public void testEmptyStringCollectionSerialization() {
//		
//		TestStringCollections obj = new TestStringCollections();
//		Document doc = new Document();
//        DocumentHelper.convertObjectToDocument(obj, doc);
//        
//        System.out.println(doc);
//        
//        List<?> values = (List<?>) doc.get("strings");
//        
//        Assert.assertNull(values);
//
//	}
//	
//	@Test
//	public void testEmptyStringCollectionDeserialization() throws ParseException {
//		
//		
//		Document doc = new Document();
//		doc.put("_clz", TestStringCollections.class.getName());
//		TestStringCollections obj = DocumentHelper.convertDocumentToObject(TestStringCollections.class, doc);
//
//        Assert.assertNull(obj.getStrings());
//	}
//	
//	@Test
//	public void testObjectSerialization() {
//		
//		Date date = new Date();
//		EmbeddedObject embedded = new EmbeddedObject("test", Long.MAX_VALUE, Integer.MIN_VALUE, Float.MAX_VALUE, Double.MIN_VALUE, date, TestEnum.FIVE, false);
//		TestFieldTypesObject obj = new TestFieldTypesObject("a", 10L, 100, 1.0F, 2.0D, date, TestEnum.THREE, true, embedded);
//		Document doc = new Document();
//        DocumentHelper.convertObjectToDocument(obj, doc);
//        
//        System.out.println(doc);
//        
//        Assert.assertEquals("a", doc.get("string"));
//        Assert.assertEquals(10L, doc.get("longNumber"));
//        Assert.assertEquals(100, doc.get("intNumber"));
//        Assert.assertEquals(1.0F, doc.get("floatNumber"));
//        Assert.assertEquals(2.0D, doc.get("doubleNumber"));
//        Assert.assertEquals(date, doc.get("date"));
//        Assert.assertEquals("THREE", doc.get("enumField"));
//        Assert.assertEquals(true, doc.get("bool"));
//        Assert.assertEquals(false, doc.get("system"));
//        Assert.assertEquals(false, doc.get("hidden"));
//        
//        @SuppressWarnings("unchecked")
//		Map<String,Object> embeddedDocument = (Map<String,Object>) doc.get("embedded");
//        Assert.assertNotNull(embeddedDocument);
//        Assert.assertEquals("test", embeddedDocument.get("embeddedString"));
//        Assert.assertEquals(Long.MAX_VALUE, embeddedDocument.get("embeddedLong"));
//        Assert.assertEquals(Integer.MIN_VALUE, embeddedDocument.get("embeddedInt"));
//        Assert.assertEquals(Float.MAX_VALUE, embeddedDocument.get("embeddedFloat"));
//        Assert.assertEquals(Double.MIN_VALUE, embeddedDocument.get("embeddedDouble"));
//        Assert.assertEquals(date, embeddedDocument.get("embeddedDate"));
//        Assert.assertEquals("FIVE", embeddedDocument.get("embeddedEnum"));
//        Assert.assertEquals(false, embeddedDocument.get("embeddedBool"));
//        Assert.assertEquals(false, embeddedDocument.get("system"));
//        Assert.assertEquals(false, embeddedDocument.get("hidden"));
//        
//	}
//	
//	@Test
//	public void testObjectDeserialization() throws ParseException {
//		
//		Date date = new Date();
//		
//		Document doc = new Document();
//		doc.put("_clz", TestFieldTypesObject.class.getName());
//        doc.put("string", "a");
//        doc.put("longNumber", 10L);
//        doc.put("intNumber", 100);
//        doc.put("floatNumber", 1.0F);
//        doc.put("doubleNumber", 2.0D);
//        doc.put("date", date);
//        doc.put("enumField", "THREE");
//        doc.put("bool", true);
//        
//        Document embeddedDoc = new Document();
//        embeddedDoc.put("_clz", EmbeddedObject.class.getName());
//        embeddedDoc.put("embeddedString", "test");
//        embeddedDoc.put("embeddedLong", Long.MAX_VALUE);
//        embeddedDoc.put("embeddedInt", Integer.MIN_VALUE);
//        embeddedDoc.put("embeddedFloat", Float.MAX_VALUE);
//        embeddedDoc.put("embeddedDouble", Double.MIN_VALUE);
//        embeddedDoc.put("embeddedDate", date);
//        embeddedDoc.put("embeddedEnum", "FIVE");
//        embeddedDoc.put("embeddedBool", false);
//
//        doc.put("embedded", embeddedDoc);
//        
//        TestFieldTypesObject obj = DocumentHelper.convertDocumentToObject(TestFieldTypesObject.class, doc);
//        
//        Assert.assertEquals("a", obj.getString());
//        Assert.assertEquals(new Long(10L), obj.getLongNumber());
//        Assert.assertEquals(new Integer(100), obj.getIntNumber());
//        Assert.assertEquals(new Float(1.0F), obj.getFloatNumber());
//        Assert.assertEquals(new Double(2.0D), obj.getDoubleNumber());
//        Assert.assertEquals(date, obj.getDate());
//        Assert.assertEquals(TestEnum.THREE, obj.getEnumField());
//        Assert.assertEquals(Boolean.TRUE, obj.getBool());
//        Assert.assertEquals(Boolean.FALSE, obj.isHidden());
//        Assert.assertEquals(Boolean.FALSE, obj.isSystem());
//        
//        Assert.assertNotNull(obj.getEmbedded());
//        
//        EmbeddedObject e = obj.getEmbedded();
//        Assert.assertEquals("test", e.getEmbeddedString());
//        Assert.assertEquals(new Long(Long.MAX_VALUE), e.getEmbeddedLong());
//        Assert.assertEquals(new Integer(Integer.MIN_VALUE), e.getEmbeddedInt());
//        Assert.assertEquals(new Float(Float.MAX_VALUE), e.getEmbeddedFloat());
//        Assert.assertEquals(new Double(Double.MIN_VALUE), e.getEmbeddedDouble());
//        Assert.assertEquals(date, e.getEmbeddedDate());
//        Assert.assertEquals(TestEnum.FIVE, e.getEmbeddedEnum());
//        Assert.assertEquals(Boolean.FALSE, e.getEmbeddedBool());
//        Assert.assertEquals(Boolean.FALSE, e.isHidden());
//        Assert.assertEquals(Boolean.FALSE, e.isSystem());
//	}
	
	
}
