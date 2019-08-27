package com.jadaptive.db;

import java.util.Date;

import com.jadaptive.repository.AbstractUUIDEntity;

public class TestObject extends AbstractUUIDEntity {

	String a;
	Long b;
	Integer c;
	Float d;
	Double e;
	Date f;
	TestEnum g;
	Boolean h;
	
	public TestObject() { }
	
	public TestObject(String a, Long b, Integer c, Float d, Double e, Date f, TestEnum g, Boolean h) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
		this.g = g;
		this.h = h;
	}
	
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public Long getB() {
		return b;
	}
	public void setB(Long b) {
		this.b = b;
	}
	public Integer getC() {
		return c;
	}
	public void setC(Integer c) {
		this.c = c;
	}
	public Float getD() {
		return d;
	}
	public void setD(Float d) {
		this.d = d;
	}
	public Double getE() {
		return e;
	}
	public void setE(Double e) {
		this.e = e;
	}
	public Date getF() {
		return f;
	}
	public void setF(Date f) {
		this.f = f;
	}
	public TestEnum getG() {
		return g;
	}
	public void setG(TestEnum g) {
		this.g = g;
	}
	public Boolean getH() {
		return h;
	}
	public void setH(Boolean h) {
		this.h = h;
	}
	
	
}
