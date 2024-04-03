package com.jadaptive.app.auth.quota;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.quotas.IPQuota;
import com.jadaptive.api.quotas.QuotaKey;
import com.jadaptive.api.quotas.QuotaService;
import com.jadaptive.api.quotas.QuotaThreshold;
import com.jadaptive.api.quotas.SessionQuota;
import com.jadaptive.api.quotas.TenantQuota;
import com.jadaptive.api.quotas.UserQuota;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.Utils;

@Service
public class QuotaServiceImpl extends AuthenticatedService implements QuotaService {

	static Logger log = LoggerFactory.getLogger(QuotaServiceImpl.class);
	
	@Autowired
	private TenantAwareObjectDatabase<Quota> objectDatbase;
	
	@Autowired
	private TenantAwareObjectDatabase<QuotaKey> keyDatabase;
	
	@Autowired
	private SystemOnlyObjectDatabase<TenantQuota> systemTenantQuotas;
	
	@Autowired
	private SystemOnlyObjectDatabase<IPQuota> systemIPQuotas;
	
	@Autowired
	private AssignableObjectDatabase<UserQuota> userQuotas;
	
	@Autowired
	private AssignableObjectDatabase<SessionQuota> sessionQuotas;
	
	@Autowired
	private TenantAwareObjectDatabase<IPQuota> ipQuotas;
	
	@Autowired
	private TenantAwareObjectDatabase<QuotaThreshold> quotaDatabase;
	
	@Autowired
	private TenantService tenantService;

	@Override
	public void registerKey(String uuid, String name) {
		
		QuotaKey key = new QuotaKey();
		key.setUuid(uuid);
		key.setName(name);
		
		keyDatabase.saveOrUpdate(key);
	}

	@Override
	public void incrementQuota(QuotaThreshold quota, String bundle, String key) throws AccessDeniedException {
		incrementQuota(quota, 1, bundle, key);
	}

	@Override
	public void incrementQuota(QuotaThreshold quota, long count, String bundle, String key) throws AccessDeniedException {
		
		if(Objects.isNull(quota)) {
			return;
		}
		
		String group = generateGroup(quota);
		
		if(quota instanceof TenantQuota) {
			tenantService.asSystem(()-> {
				incremenentTeantQuota(quota, group, count, bundle, key);
			});
		} else {
			incremenentTeantQuota(quota, group, count, bundle, key);
		}
	}
	
	private String generateGroup(QuotaThreshold quota) {
		
		String group;
		switch(quota.getResourceKey()) {
		case IPQuota.RESOURCE_KEY:
			group = String.format("ip/%s", Request.getRemoteAddress());
			break;
		case SessionQuota.RESOURCE_KEY:
			group = String.format("session/%s", Request.get().getSession().getId());
			break;
		case TenantQuota.RESOURCE_KEY:
			group = String.format("tenant/%s", getCurrentTenant().getUuid());
			break;
		default:
			group = String.format("user/%s", getCurrentUser().getUuid());
		}
		
		return group;
	}

	private void incremenentTeantQuota(QuotaThreshold quota, String group, long count, String bundle, String key) {

		Quota q = internalAssert(quota, bundle, key);
		
		q.setValue(q.getValue() + count);
		objectDatbase.saveOrUpdate(q);
		
		internalAssert(q, quota, bundle, key);
	}


	@Override
	public boolean hasKey(String uuid) {
		return keyDatabase.count(QuotaKey.class, SearchField.eq("uuid", uuid)) > 0;
	}

	@Override
	public QuotaKey getKey(String uuid) {
		return keyDatabase.get(uuid, QuotaKey.class);
	}

	@Override
	public QuotaThreshold getAssignedThreshold(QuotaKey key) {
		return getLowestThreshold(key);
	}
	
	private QuotaThreshold getLowestThreshold(QuotaKey key) {
		
		
		QuotaThreshold minimumValue = null;
		
		if(hasUserContext()) {
			for(QuotaThreshold q : userQuotas.getAssignedObjects(UserQuota.class,
					getCurrentUser(), 
					SearchField.eq("resourceKey", UserQuota.RESOURCE_KEY),
					SearchField.in("key.uuid", key.getUuid()))) {
				if(Objects.isNull(minimumValue)) {
					minimumValue = q;
					continue;
				}
				if(Utils.fromByteSize(minimumValue.getValue()) > Utils.fromByteSize(q.getValue())) {
					minimumValue = q;
				}
			}
			
			for(QuotaThreshold q : sessionQuotas.getAssignedObjects(SessionQuota.class,
					getCurrentUser(), 
					SearchField.eq("resourceKey", SessionQuota.RESOURCE_KEY),
					SearchField.in("key.uuid", key.getUuid()))) {
				if(Objects.isNull(minimumValue)) {
					minimumValue = q;
					continue;
				}
				if(Utils.fromByteSize(minimumValue.getValue()) > Utils.fromByteSize(q.getValue())) {
					minimumValue = q;
				}
			}
		}
		
		for(TenantQuota q : systemTenantQuotas.searchObjects(TenantQuota.class,
				SearchField.eq("resourceKey", TenantQuota.RESOURCE_KEY),
				SearchField.in("tenant.uuid", getCurrentTenant().getUuid()), 
				SearchField.in("key.uuid", key.getUuid()))) {
			if(q.getAllTenants()) {
				if(Objects.isNull(minimumValue)) {
					minimumValue = q;
					continue;
				}
				if(Utils.fromByteSize(minimumValue.getValue()) > Utils.fromByteSize(q.getValue())) {
					minimumValue = q;
				}
			}
		}
		
		for(IPQuota q : ipQuotas.searchObjects(IPQuota.class,
				SearchField.eq("resourceKey", IPQuota.RESOURCE_KEY),
				SearchField.in("key.uuid", key.getUuid()))) {
			if(q.getAllAddresses() ||  matchesIPAddress(q, Request.getRemoteAddress())) {
			
				if(Objects.isNull(minimumValue)) {
					minimumValue = q;
					continue;
				}
				if(Utils.fromByteSize(minimumValue.getValue()) > Utils.fromByteSize(q.getValue())) {
					minimumValue = q;
				}
			}
		}
		
		for(IPQuota q : systemIPQuotas.list(IPQuota.class,
				SearchField.eq("resourceKey", IPQuota.RESOURCE_KEY),
				SearchField.in("key.uuid", key.getUuid()))) {
			if(q.getAllAddresses() ||  matchesIPAddress(q, Request.getRemoteAddress())) {
			
				if(Objects.isNull(minimumValue)) {
					minimumValue = q;
					continue;
				}
				if(Utils.fromByteSize(minimumValue.getValue()) > Utils.fromByteSize(q.getValue())) {
					minimumValue = q;
				}
			}
		}

		return minimumValue;
	}

	private boolean matchesIPAddress(IPQuota q, String remoteAddress) {
		/**
		 * TODO CIDR support
		 */
		for(String ipAddress : q.getIpAddresses()) {
			if(remoteAddress.equals(ipAddress)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void createQuota(IPQuota quota) {
		quotaDatabase.saveOrUpdate(quota);
	}

	@Override
	public void assertQuota(QuotaThreshold quota, String bundle, String key) {
		
		if(Objects.isNull(quota)) {
			return;
		}
		
		internalAssert(quota, bundle, key);
		
	}
	
	private Quota internalAssert(QuotaThreshold quota, String bundle, String key) {
		return internalAssert(getQuota(quota), quota, bundle, key);
	}
	
	private Quota internalAssert(Quota q, QuotaThreshold quota, String bundle, String key) {
		if(q.getValue() > q.getQuota()) {
			if(log.isInfoEnabled()) {
				log.info("Rejecting access to group {} on key {} for exceeding quota of {} every {} seconds", q.getGroup(), quota.getKey().getName(), quota, quota.getPeriodUnit() + quota.getPeriodUnit().toString());
			}
			throw new ObjectException(bundle, key, quota.getValue().toUpperCase(), String.format("%d %s", quota.getPeriodValue(), quota.getPeriodUnit().name()));
		}
		
		return q;
		
	}

	private Quota getQuota(QuotaThreshold quota) {
		
		Quota q;
		String group = generateGroup(quota);
		try {
			q = objectDatbase.get(Quota.class, 
					SearchField.eq("group", group),
					SearchField.eq("key.uuid", quota.getKey().getUuid()));
		} catch (ObjectException e) {
			q = new Quota();
			q.setKey(quota.getKey());
			q.setGroup(group);
			q.setValue(0);
			q.setQuota(Utils.fromByteSize(quota.getValue()));
			q.setPeriod(quota.getPeriodUnit().toMillis(quota.getPeriodValue()));
			q.setQuotaStarted(new Date());
			
			if(log.isInfoEnabled()) {
				log.info("Created quota for group {} on key {} to {} every {} {}", group, quota.getKey().getName(), quota, quota.getPeriodValue(), quota.getPeriodUnit().toString());
			}
		}
		
		if(System.currentTimeMillis() - q.getQuotaStarted().getTime() > q.getPeriod()) {
			q.setQuotaStarted(new Date());
			q.setValue(0L);
			
			if(log.isInfoEnabled()) {
				log.info("Reset quota for group {} on key {} to {} every {} {}", group, quota.getKey().getName(), quota, quota.getPeriodValue(), quota.getPeriodUnit().toString());
			}
			
			objectDatbase.saveOrUpdate(q);
		}
		
		return q;
	}

	@Override
	public long getUsedQuota(QuotaThreshold quota) {
		
		if(Objects.isNull(quota)) {
			return 0L;
		}
		
		Quota q = getQuota(quota);
		return Math.min(q.getValue(), q.getQuota());
		
	}
	
	@Override
	public long getRemainingQuota(QuotaThreshold quota) {
		
		if(Objects.isNull(quota)) {
			return 0L;
		}
		
		Quota q = getQuota(quota);
		return Math.max(q.getQuota() - q.getValue(), 0L);
		
	}
	
	
	
}
