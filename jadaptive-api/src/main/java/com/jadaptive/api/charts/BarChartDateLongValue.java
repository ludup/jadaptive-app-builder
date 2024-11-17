package com.jadaptive.api.charts;

import java.util.Date;

public class BarChartDateLongValue {

	Long date;
	Long value;
	
	public BarChartDateLongValue(Date date, Long value) {
		this.date = date.getTime();
		this.value = value;
	}

	
	public Long getDate() {
		return date;
	}


	public void setDate(Long date) {
		this.date = date;
	}


	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
	
}
