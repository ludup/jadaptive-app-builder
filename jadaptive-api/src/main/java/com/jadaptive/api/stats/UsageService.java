package com.jadaptive.api.stats;

import java.util.Date;

public interface UsageService {

	void log(long value, String... keys);

	Long sum(String key, Date from, Date to);
	
	Long sumOr(Date from, Date to, String... keys);

	void incrementDailyValue(String key, long val);
	
	void incrementDailyValue(String key);

	long getDailyValue(String uuid);

	Long sumAnd(Date from, Date to, String... keys);

	void setMonthlyValue(String key, Date date, long val);

	long getMonthlyValue(String key, Date date);

	void setDailyValue(String key, Date date, long val);

	void setDailyValue(String key, long val);

	Long sum(String key);

	void delete(String key);

	Long sumAnd(String... keys);

	Long sumOr(String... keys);

	Long getDailyValue(String generateUsageKey, Date from);

}
