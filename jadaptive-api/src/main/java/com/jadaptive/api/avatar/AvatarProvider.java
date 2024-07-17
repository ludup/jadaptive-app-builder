package com.jadaptive.api.avatar;

import java.util.Optional;

import org.pf4j.ExtensionPoint;

public interface AvatarProvider extends ExtensionPoint {
	
	default int avatarWeight() {
		return 0;
	}

	Optional<Avatar> find(AvatarRequest request);
}
