package com.jadaptive.api.cluster;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Net {

	public static String getLocalHostName() {
		return getBestLocalNic().map(InetAddress::getHostName).orElseGet(() -> {
			try {
				return InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				return InetAddress.getLoopbackAddress().getHostName();
			}
		});
	}
	
	public static String getLocalHostAddress() {
		return getBestLocalNic().map(InetAddress::getHostAddress).orElseGet(() -> {
			try {
				return InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				return InetAddress.getLoopbackAddress().getHostAddress();
			}
		});
	}

	public static Optional<InetAddress> getBestLocalNic() {
		return getBestLocalNics().stream().findFirst();
	}
    
	public static List<InetAddress> getBestLocalNics() {
		var addrList = new ArrayList<InetAddress>();
		try {
			for (var nifEn = NetworkInterface.getNetworkInterfaces(); nifEn
					.hasMoreElements();) {
				var nif = nifEn.nextElement();
				if (!nif.isLoopback() && nif.isUp()) {
					for (var addr : nif.getInterfaceAddresses()) {
						var ipAddr = addr.getAddress();
						if (!ipAddr.isAnyLocalAddress() && !ipAddr.isLinkLocalAddress()
								&& !ipAddr.isLoopbackAddress()) {
							addrList.add(ipAddr);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return Collections.unmodifiableList(addrList);
	}
}
