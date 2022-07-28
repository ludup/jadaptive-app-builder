package com.jadaptive.api.stats;

import java.util.Date;

public interface UsageService {

	String HTTP_OUT = "http_out";

	void log(long value, String... keys);

	Long sum(String key, Date from, Date to);
	
	Long sumOr(Date from, Date to, String... keys);

	void incrementDailyValue(String key, long byValue);
	
	void incrementDailyValue(String key);

	long getDailyValue(String uuid);

	Long sumAnd(Date from, Date to, String... keys);

}
