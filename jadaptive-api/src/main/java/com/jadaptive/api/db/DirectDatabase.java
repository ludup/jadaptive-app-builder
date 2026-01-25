package com.jadaptive.api.db;

public interface DirectDatabase {

	long getNextSequence(String table, String database, String sequenceName);

	

}
