package com.jadaptive.app.tenant;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.charts.BarChartDateLongValue;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.stats.DailyCounter;
import com.jadaptive.api.stats.MonthlyCounter;
import com.jadaptive.api.stats.StatsConfiguration;
import com.jadaptive.api.stats.Usage;
import com.jadaptive.api.stats.UsageService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.Utils;

@Service
public class UsageServiceImpl implements UsageService, TenantAware {
	
	private final static Logger LOG = LoggerFactory.getLogger(UsageServiceImpl.class);

	@Autowired
	private TenantAwareObjectDatabase<Usage> usageDatabase;
	
	@Autowired
	private TenantAwareObjectDatabase<DailyCounter> dailyDatabase;
	
	@Autowired
	private TenantAwareObjectDatabase<MonthlyCounter> monthlyDatabase;
	
	@Autowired
	private SingletonObjectDatabase<StatsConfiguration> config;
	
	@Autowired
	private TenantService tenantService;
	
	@Autowired
	private EventService eventService;
	
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private Map<String, ScheduledFuture<?>> tenantCleanUpTasks = new ConcurrentHashMap<>();
	
	@PostConstruct
	private void onConstruct() {
		eventService.eventRegistrations(() -> {
			eventService.deleted(Tenant.class, (evt)-> {
				var tsk = tenantCleanUpTasks.get(evt.getObject().getUuid());
				if(tsk != null)
					tsk.cancel(false);
			});;
		});
	}
	
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
	public synchronized Long getDailyValue(String key, Date from) {
		
		try {
			DailyCounter counter = dailyDatabase.get(DailyCounter.class,
					SearchField.eq("date", from),
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
	public Stream<BarChartDateLongValue> values(Date from, Date to, String... keys) {
		return usageDatabase.searchObjects(Usage.class,
				SearchField.all("keys", Arrays.asList(keys)),
				SearchField.gte("created", from),
				SearchField.lt("created", to)).stream().map(o -> {
					return new BarChartDateLongValue(o.getCreated(), o.getValue());
				});
	}
	
	@Override
	public Long sum(String key, Date from, Date to) {
		return usageDatabase.sumLongValues(Usage.class, "value", 
				SearchField.in("keys", key), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}
	
	@Override
	public Long sumOfDailyValues(String key, Date from, Date to) {
		return dailyDatabase.sumLongValues(DailyCounter.class, "value", 
				SearchField.in("key", key), 
				SearchField.gte("date", from),
				SearchField.lt("date", to));
	}
	
	@Override
	public Long sumOfDailyValues(String key) {
		return dailyDatabase.sumLongValues(DailyCounter.class, "value", 
				SearchField.in("key", key));
	}

	
	@Override
	public Long sum(String key) {
		return usageDatabase.sumLongValues(Usage.class, "value", 
				SearchField.in("keys", key));
	}

	
	@Override
	public Long sumOr(Date from, Date to, String... keys) {
		return usageDatabase.sumLongValues(Usage.class, "value", 
				SearchField.in("keys", Arrays.asList(keys)), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}
	
	@Override
	public Long sumOr(String... keys) {
		return usageDatabase.sumLongValues(Usage.class, "value", 
				SearchField.in("keys", Arrays.asList(keys)));
	}
	
	@Override
	public Long sumAnd(Date from, Date to, String... keys) {
		return usageDatabase.sumLongValues(Usage.class, "value", 
				SearchField.all("keys", Arrays.asList(keys)), 
				SearchField.gte("created", from),
				SearchField.lt("created", to));
	}
	
	@Override
	public Long sumAnd(String... keys) {
		return usageDatabase.sumLongValues(Usage.class, "value", 
				SearchField.all("keys", Arrays.asList(keys)));
	}
	
	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		schedule(tenant);
	}
	
	private void cleanUp(Tenant tenant) {
		
		/* Clean up daily around midnight, but spread this out  between the hour between 12am and 1am.
		 * Each tenant is assigned a minute of the hour they are always scheduled for (% 60). 
		 */
		var statsConfig = config.getObject(StatsConfiguration.class);
		var days = Math.max(0, statsConfig.getDaysToRetain());
		if(days == 0) {
			LOG.warn("Not cleaning up usage statistics for {}, days to retain is ZERO. Data will accumulate forever.");
		}
		else {
			
			LOG.info("Cleaning up usage statistic for {}", tenant.getName());
			
			var z = ZoneId.systemDefault();
			var started = System.currentTimeMillis();
			var start = ZonedDateTime.now( z ).toLocalDate().minusDays(days);
	
			LOG.info("Delete usage older than {} ({} day(s))", start, days);
			usageDatabase.delete(Usage.class, SearchField.lt("created", Date.from(start.atStartOfDay()
				      .atZone(ZoneId.systemDefault())
				      .toInstant())));
			LOG.info("Cleaned up usage statistic for {}, took {}", tenant.getName(), System.currentTimeMillis() - started);
		}
		
		/* Schedule next one */
		schedule(tenant);
	}

	private void schedule(Tenant tenant) {
		var z = ZoneId.systemDefault();
		var now = ZonedDateTime.now( z );
		var tomorrow = now.toLocalDate().plusDays(1);
		var tomorrowStart = tomorrow.atStartOfDay( z );
		
		tenantCleanUpTasks.put(tenant.getUuid(), executor.schedule(() -> {
			/* Make sure new task runs with an updated tenant (at the point it runs) */
			tenantService.asSystem(() -> {
				var ten = tenantService.getTenantByUUID(tenant.getUuid());
				tenantService.executeAs(tenant, () -> {
					cleanUp(ten);
				});
			});
		}, Duration.between(now, tomorrowStart).toMinutes() + (Integer.toUnsignedLong(tenant.getUuid().hashCode()) % 60),
				TimeUnit.MINUTES));
	}
}
