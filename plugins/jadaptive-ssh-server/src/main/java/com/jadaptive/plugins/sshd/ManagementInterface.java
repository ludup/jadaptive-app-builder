package com.jadaptive.plugins.sshd;

/**
 * Currently unused.
 */
public class ManagementInterface extends SSHInterface {

	private static final long serialVersionUID = 6046127484796461110L;

	public static final String RESOURCE_KEY = "managementInterface";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public Class<? extends SSHInterfaceFactory<?,?>> getInterfaceFactory() {
		return null;
	}

}
