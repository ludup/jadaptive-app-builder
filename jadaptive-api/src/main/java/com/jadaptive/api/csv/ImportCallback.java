package com.jadaptive.api.csv;

public interface ImportCallback {

	void imported(long count, String... values);
}
