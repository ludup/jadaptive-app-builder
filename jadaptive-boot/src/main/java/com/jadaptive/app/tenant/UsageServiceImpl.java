package com.jadaptive.app.tenant;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.stats.DailyCounter;
import com.jadaptive.api.stats.MonthlyCounter;
import com.jadaptive.api.stats.Usage;
import com.jadaptive.api.stats.UsageService;
import com.jadaptive.utils.Utils;

@Service
public class UsageServiceImpl implements UsageService {

	@Autowired
	private TenantAwareObjectDatabase<Usage> usageDatabase;
	
	@Autowired
	private TenantAwareObjectDatabase<DailyCounter> dailyDatabase;
	
	@Autowired
	private TenantAwareObjectDatabase<MonthlyCounter> monthlyDatabase;
	
	@Override
	public void log(long value, String... keys) {
		
		Usage usage = new Usage();
		usage.setKeys(Arrays.asList(keys));
		usage.setValue(value);
		usage.setTimestamp(Utils.now());
		
		usageDatabase.saveOrUpdate(usage);
	}
	
	@Override
	public void delete(String key) {
		for(Usage usage : usageDatabase.searchObjects(Usage.class, SearchField.all("keys", key))) {
			usageDatabase.delete(usage);
		}
	}
	
	@Override
	public void incrementDailyValue(String key) {
		incrementDailyValue(key, 1L);
	}
	
	@Override
	public synchronized void incrementDailyValue(String key, long byValue) {
		
		DailyCounter counter;
		
		try {
			counter = dailyDatabase.get(DailyCounter.class,
					SearchField.eq("date", Utils.today()),
					SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			counter = new DailyCounter();
			counter.setDate(Utils.today());
			counter.setValue(0);
			counter.setKey(key);
		}
		
		counter.setValue(counter.getValue() + byValue);
		dailyDatabase.saveOrUpdate(counter);
		
	}
	
	@Override
	public synchronized long getDailyValue(String key) {
		
		try {
			DailyCounter counter = dailyDatabase.get(DailyCounter.class,
					SearchField.eq("date", Utils.today()),
					SearchField.eq("key", key));
			return counter.getValue();
		} catch(ObjectNotFoundException e) {
			return 0L;
		}
	}
	
	@Override
	public synchronized long getMonthlyValue(String key, Date date) {
		
		try {
			MonthlyCounter counter = monthlyDatabase.get(MonthlyCounter.class,
					SearchField.eq("date", Utils.getMonthEnd(date)),
					SearchField.eq("key", key));
			return counter.getValue();
		} catch(ObjectNotFoundException e) {
			return 0L;
		}
	}
	
	@Override
	public synchronized void setMonthlyValue(String key, Date date, long byValue) {
		
		MonthlyCounter counter;
		Date monthEnd = Utils.getMonthEnd(date);
		try {
			counter = monthlyDatabase.get(MonthlyCounter.class,
					SearchField.eq("date",monthEnd),
					SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			counter = new MonthlyCounter();
			counter.setDate(monthEnd);
			counter.setValue(0);
			counter.setKey(key);
		}
		
		counter.setValue(byValue);
		monthlyDatabase.saveOrUpdate(counter);
		
	}
	
	@Override
	public synchronized void setDailyValue(String key, long val) {
		setDailyValue(key, Utils.today(), val);
	}
	
	@Override
	public synchronized void setDailyValue(String key, Date date, long byValue) {
		
		DailyCounter counter;
		
		try {
			counter = dailyDatabase.get(DailyCounter.class,
					SearchField.eq("date", date),
					SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			counter = new DailyCounter();
			counter.setDate(Utils.today());
			counter.setValue(0);
			counter.setKey(key);
		}
		
		counter.setValue(byValue);
		dailyDatabase.saveOrUpdate(counter);
		
	}
	
	@Override
	public Long sum(String key, Date from, Date to) {
		return usageDatabase.sum(Usage.class, "value", 
				SearchField.in("keys", key), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}

	
	@Override
	public Long sum(String key) {
		return usageDatabase.sum(Usage.class, "value", 
				SearchField.in("keys", key));
	}

	
	@Override
	public Long sumOr(Date from, Date to, String... keys) {
		return usageDatabase.sum(Usage.class, "value", 
				SearchField.in("keys", Arrays.asList(keys)), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}
	
	@Override
	public Long sumAnd(Date from, Date to, String... keys) {
		return usageDatabase.sum(Usage.class, "value", 
				SearchField.all("keys", Arrays.asList(keys)), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}
}
