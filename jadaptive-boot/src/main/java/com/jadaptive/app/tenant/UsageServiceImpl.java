package com.jadaptive.app.tenant;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.stats.Usage;
import com.jadaptive.api.stats.UsageService;

@Service
public class UsageServiceImpl implements UsageService {

	@Autowired
	private TenantAwareObjectDatabase<Usage> usageDatabase;
	
	@Override
	public void log(long value, String... keys) {
		
		Usage usage = new Usage();
		usage.setKeys(Arrays.asList(keys));
		usage.setValue(value);
		
		usageDatabase.saveOrUpdate(usage);
	}
	
	public Long sum(String key) {
		return usageDatabase.sum(Usage.class, "value", SearchField.in("keys", key));
	}
}
