package com.jadaptive.app.tenant;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.app.App;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.charts.BarChartDateLongValue;
import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.stats.UsageQueryConfiguration;
import com.jadaptive.api.stats.UsageService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UsageController extends AuthenticatedController {
	
	@Autowired
	private UsageService usageService; 
	
	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private TenantService tenantService;
	
	@RequestMapping(value="/app/api/usage/daily/{key}/{days}/", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public ResourceStatus<BarChartDateLongValue[]> getDailyInstances(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String key, @PathVariable Integer days)
			throws IOException {
		
		var queryDays = checkMaxQueryDays(days);
		return new ResourceStatus<>(cacheOrSupply(request, String.format("daily.%d.%s", queryDays, key), () -> {

			int hint = Integer.parseInt(StringUtils.defaultIfEmpty(Request.get().getParameter("hint"), "10"));
			var revenue = new ArrayList<BarChartDateLongValue>();
			var from = Calendar.getInstance();
			from.setTime(DateUtils.addDays(Utils.today(), -(queryDays)));

			while(from.before(Utils.tomorrowCalendar())) {
					
				if(Boolean.getBoolean("jadaptive.development")) {
					if(from.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || from.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
						revenue.add(new BarChartDateLongValue(from.getTime(), new Random().nextLong(0, hint / 2)));
					} else {
						revenue.add(new BarChartDateLongValue(from.getTime(), new Random().nextLong(0, hint)));
					}
				} else {
					revenue.add(new BarChartDateLongValue(from.getTime(), usageService.getDailyValue(key, from.getTime())));
				}
					
				from.setTime(DateUtils.addDays(from.getTime(), 1));
			}
			
			return revenue.toArray(new BarChartDateLongValue[0]);
			
		}, queryDays, key));
	}
	
	@RequestMapping(value="/app/api/usage/sum/days/{keys}/{days}/", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public ResourceStatus<BarChartDateLongValue[]> sumDailyValues(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String keys, @PathVariable Integer days)
			throws IOException {
		
		var queryDays = checkMaxQueryDays(days);
		
		return new ResourceStatus<>(cacheOrSupply(request, String.format("sumByDay.%d.%s", queryDays, keys), () -> {
			
			var revenue = new ArrayList<BarChartDateLongValue>();
			var hint = Integer.parseInt(StringUtils.defaultIfEmpty(Request.get().getParameter("hint"), "10"));

			var from = Calendar.getInstance();
			from.setTime(DateUtils.addDays(Utils.today(), -queryDays));

			var to = DateUtils.addDays(from.getTime(), 1);
			
			while(from.before(Utils.tomorrowCalendar())) {
					
				if(Boolean.getBoolean("jadaptive.development")) {
					if(from.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || from.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
						revenue.add(new BarChartDateLongValue(from.getTime(), new Random().nextLong(0, hint / 2)));
					} else {
						revenue.add(new BarChartDateLongValue(from.getTime(), new Random().nextLong(0, hint)));
					}
				} else {
					revenue.add(new BarChartDateLongValue(from.getTime(), usageService.sumAnd(from.getTime(), to, keys.split(","))));
				}
				
				to = from.getTime();
				from.setTime(DateUtils.addDays(from.getTime(), 1));
			}
			
			return revenue.toArray(new BarChartDateLongValue[0]);
			
		}, queryDays, keys));
	}
	
	@RequestMapping(value="/app/api/usage/all/{keys}/{days}", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public ResourceStatus<BarChartDateLongValue[]> allValues(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String keys, @PathVariable Integer days)
			throws IOException {
		var queryDays = checkMaxQueryDays(days);

		return new ResourceStatus<>(cacheOrSupply(request, String.format("all.%d.%s", queryDays, keys), () -> {
			
			var from = Calendar.getInstance();
			var today = Utils.today();
			from.setTime(DateUtils.addDays(today, -queryDays));
			var to = DateUtils.addDays(today, 1);
			
			return usageService.values(from.getTime(), to, keys.split(",")).toList().toArray(new BarChartDateLongValue[0]);
			
		}, queryDays, keys));
	}
	
	private BarChartDateLongValue[] cacheOrSupply(HttpServletRequest request, String key, Supplier<BarChartDateLongValue[]> supplier, int days, String keys) {

		setupUserContext(request);
		try {
			var cache = cacheService.getCacheOrCreate(key, Date.class, BarChartDateLongValue[].class, Duration.ofHours(1).toMillis());
			var values = (BarChartDateLongValue[])cache.get(Utils.today());
			
			if(Objects.isNull(values)) {
				values = supplier.get();
				
				if(!Boolean.getBoolean("jadaptive.development")) {
					cache.put(Utils.today(), values);
				}
			}

			return values;
		}
		finally {
			clearUserContext();
		}
	}
	
	private int checkMaxQueryDays(int days) {
		if(tenantService.isSystemTenant()) {
			/* System tenant always gets all  usage data */
			return days;
		}
		else {
			
			try {
				/** Product may define this **/
				UsageQueryConfiguration conf = App.bean(UsageQueryConfiguration.class);
				return conf.getMaxQueryDays(days);
			} catch(NoSuchBeanDefinitionException e) {
				return days;
			}
			
		}
	}
}

