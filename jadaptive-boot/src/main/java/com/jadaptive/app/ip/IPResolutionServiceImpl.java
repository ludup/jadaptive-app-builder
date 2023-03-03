package com.jadaptive.app.ip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.SystemSingletonObjectDatabase;
import com.jadaptive.api.ip.IPResolutionService;
import com.jadaptive.api.ip.IPStackConfiguration;
import com.jadaptive.api.json.IStackLocation;

@Service
public class IPResolutionServiceImpl implements IPResolutionService {

	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private SystemSingletonObjectDatabase<IPStackConfiguration> ipConfig;
	
	@Override
	public IStackLocation resolveIPAddress(String ipAddress) throws IOException {
		try {
			
			Map<String, IStackLocation> cached = cacheService.getCacheOrCreate(
					"geoIPs", String.class, IStackLocation.class,  
					TimeUnit.DAYS.toMillis(7));
			
			IStackLocation loc = cached.get(ipAddress);
			if(Objects.nonNull(loc)) {
				return loc;
			}
			
			ObjectMapper o = new ObjectMapper();
			IPStackConfiguration ipStack = ipConfig.getObject(IPStackConfiguration.class);
			
			String accessKey = ipStack.getApiKey();
			
			if(StringUtils.isBlank(accessKey)) {
				throw new IOException("No ipstack.com access key configured");
			}
			URL url = new URL(String.format("http://api.ipstack.com/%s?access_key=" + accessKey, ipAddress));
			try(InputStream in = url.openStream()) {
				String locationJson = IOUtils.toString(in, "UTF-8");
				return o.readValue(locationJson, IStackLocation.class);
			}

		} catch(IllegalStateException e) {
			throw new IOException("No ipstack.com access key configured");
		}
	}
	
	
	
}
