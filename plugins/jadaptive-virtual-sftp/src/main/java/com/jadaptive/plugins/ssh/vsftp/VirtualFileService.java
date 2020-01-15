package com.jadaptive.plugins.ssh.vsftp;

import com.jadaptive.api.user.User;

public interface VirtualFileService {

	boolean checkMountExists(String mount, User user);

	boolean checkSupportedMountType(String type);

}
