package com.jadaptive.app.tenant;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.charts.BarChartDateLongValue;
import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.stats.UsageService;
import com.jadaptive.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UsageController extends AuthenticatedController {

	
	@Autowired
	private UsageService usageService; 
	
	@Autowired
	private CacheService cacheService;
	
	@RequestMapping(value="/app/api/usage/daily/{key}/{days}", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public ResourceStatus<BarChartDateLongValue[]> getDailyInstances(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String key, @PathVariable(required = false, value = "7") Integer days)
			throws IOException {

		setupUserContext(request);
		
		try {
			
			@SuppressWarnings("rawtypes")
			Map<Date, List> cache = cacheService.getCacheOrCreate(String.format("daily.%d.%s", days, key), Date.class, List.class, Duration.ofDays(1).toMillis());
			
			@SuppressWarnings("unchecked")
			List<BarChartDateLongValue> revenue = cache.get(Utils.today());
			if(Objects.isNull(revenue)) {
				
				revenue = new ArrayList<>();

				Date from = DateUtils.addDays(Utils.today(), -90);
				
				while(from.before(Utils.today())) {
						
					if(Boolean.getBoolean("jadaptive.development")) {
						if(from.getDay() == Calendar.SUNDAY || from.getDay() == Calendar.SATURDAY) {
							revenue.add(new BarChartDateLongValue(from, new Random().nextLong(0, 3)));
						} else {
							revenue.add(new BarChartDateLongValue(from, new Random().nextLong(0, 10)));
						}
					} else {
						revenue.add(new BarChartDateLongValue(from, usageService.getDailyValue(key, from)));
					}
						
					from = DateUtils.addDays(from, 1);
				}
				
				if(!Boolean.getBoolean("jadaptive.development")) {
					cache.put(Utils.today(), revenue);
				}
			}
			
			return new ResourceStatus<BarChartDateLongValue[]>(revenue.toArray(new BarChartDateLongValue[0]));
		} finally {
			clearUserContext();
		}
	}
	
	@RequestMapping(value="/app/api/usage/sum/days/{keys}/{days}/", method = RequestMethod.GET, produces = {"application/json;charset-UTF-8"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public ResourceStatus<BarChartDateLongValue[]> sumDailyValues(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String keys, @PathVariable Integer days)
			throws IOException {

		setupUserContext(request);
		
		try {
			
			@SuppressWarnings("rawtypes")
			Map<Date, List> cache = cacheService.getCacheOrCreate(String.format("sumByDay.%d.%s", days, keys), Date.class, List.class, Duration.ofDays(1).toMillis());
			
			@SuppressWarnings("unchecked")
			List<BarChartDateLongValue> revenue = cache.get(Utils.today());
			if(Objects.isNull(revenue)) {
				
				revenue = new ArrayList<>();

				Date from = DateUtils.addDays(Utils.today(), -days);
				Date to = DateUtils.addDays(from, 1);
				
				while(from.before(Utils.today())) {
						
					if(Boolean.getBoolean("jadaptive.development")) {
						if(from.getDay() == Calendar.SUNDAY || from.getDay() == Calendar.SATURDAY) {
							revenue.add(new BarChartDateLongValue(from, new Random().nextLong(0, 3)));
						} else {
							revenue.add(new BarChartDateLongValue(from, new Random().nextLong(0, 10)));
						}
					} else {
						revenue.add(new BarChartDateLongValue(from, usageService.sumAnd(from, to, keys.split(","))));
					}
					
					to = from;
					from = DateUtils.addDays(from, 1);
				}
				
				if(!Boolean.getBoolean("jadaptive.development")) {
					cache.put(Utils.today(), revenue);
				}
			}
			
			return new ResourceStatus<BarChartDateLongValue[]>(revenue.toArray(new BarChartDateLongValue[0]));
		} finally {
			clearUserContext();
		}
	}
}

