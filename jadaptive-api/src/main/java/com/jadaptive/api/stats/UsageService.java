package com.jadaptive.api.stats;

import java.util.Date;

public interface UsageService {

	void log(long value, String... keys);

	Long sum(String key, Date from, Date to);
	
	Long sum(Date from, Date to, String... keys);

}
