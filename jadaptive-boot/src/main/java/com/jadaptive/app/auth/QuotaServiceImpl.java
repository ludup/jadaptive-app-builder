package com.jadaptive.app.auth;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.AccessDeniedException;

@Service
public class QuotaServiceImpl implements QuotaService {

	static Logger log = LoggerFactory.getLogger(QuotaServiceImpl.class);
	
	@Autowired
	TenantAwareObjectDatabase<Quota> objectDatbase;
	
	@Override
	public void incrementQuota(String group, String key, long quota, long period) throws AccessDeniedException {
		incrementQuota(group, key, 1, quota, period);
	}
	
	@Override
	public void incrementQuota(String group, String key, long count, long quota, long period) throws AccessDeniedException {
		
		Quota q;
		
		try {
			q = objectDatbase.get(Quota.class, SearchField.eq("key", key),
					SearchField.eq("group", group));
		} catch (EntityException e) {
			q = new Quota();
			q.setGroup(group);
			q.setKey(key);
			q.setValue(count);
			q.setQuota(quota);
			q.setPeriod(period);
			q.setQuotaStarted(new Date());
			
			if(log.isInfoEnabled()) {
				log.info("Created quota for group {} on key {} to {} every {} seconds", group, key, quota, period);
			}
		}
		
		while(System.currentTimeMillis() - q.getQuotaStarted().getTime() > (period * 1000L)) {
			q.setQuotaStarted(new Date(q.getQuotaStarted().getTime()+(period * 1000L)));
			q.setValue(0L);
			
			if(log.isInfoEnabled()) {
				log.info("Reset quota for group {} on key {} to {} every {} seconds", group, key, quota, period);
			}
		}
		
		q.setValue(q.getValue() + count);
		
		if(q.getValue() > q.getQuota()) {
			if(log.isInfoEnabled()) {
				log.info("Rejecting access to group {} on key {} for exceeding quota of {} every {} seconds", group, key, quota, period);
			}
			throw new AccessDeniedException();
		}
		
		objectDatbase.saveOrUpdate(q);
	}
}
