package com.jadaptive.app.tenant;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.stats.Counter;
import com.jadaptive.api.stats.Usage;
import com.jadaptive.api.stats.UsageService;
import com.jadaptive.utils.Utils;

@Service
public class UsageServiceImpl implements UsageService {

	@Autowired
	private TenantAwareObjectDatabase<Usage> usageDatabase;
	
	@Autowired
	private TenantAwareObjectDatabase<Counter> counterDatabase;
	
	@Override
	public void log(long value, String... keys) {
		
		Usage usage = new Usage();
		usage.setKeys(Arrays.asList(keys));
		usage.setValue(value);
		
		usageDatabase.saveOrUpdate(usage);
	}
	
	@Override
	public void incrementDailyValue(String key) {
		incrementDailyValue(key, 1L);
	}
	
	@Override
	public void incrementDailyValue(String key, long byValue) {
		
		Counter counter;
		
		try {
			counter = counterDatabase.get(Counter.class,
					SearchField.eq("date", Utils.today()),
					SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			counter = new Counter();
			counter.setDate(Utils.today());
			counter.setValue(0);
		}
		
		counter.setValue(counter.getValue() + byValue);
		counterDatabase.saveOrUpdate(counter);
		
	}
	
	@Override
	public long getDailyValue(String key) {
		
		try {
			Counter counter = counterDatabase.get(Counter.class,
					SearchField.eq("date", Utils.today()),
					SearchField.eq("key", key));
			return counter.getValue();
		} catch(ObjectNotFoundException e) {
			return 0L;
		}
	}
	
	@Override
	public Long sum(String key, Date from, Date to) {
		return usageDatabase.sum(Usage.class, "value", 
				SearchField.in("keys", key), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}

	@Override
	public Long sum(Date from, Date to, String... keys) {
		return usageDatabase.sum(Usage.class, "value", 
				SearchField.in("keys", Arrays.asList(keys)), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}
}
