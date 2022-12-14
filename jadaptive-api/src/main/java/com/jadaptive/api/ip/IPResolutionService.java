package com.jadaptive.api.ip;

import java.io.IOException;

import com.jadaptive.api.json.IStackLocation;

public interface IPResolutionService {

	IStackLocation resolveIPAddress(String ipAddress) throws IOException;

}
