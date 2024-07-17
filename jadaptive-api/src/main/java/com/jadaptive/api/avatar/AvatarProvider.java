package com.jadaptive.api.avatar;

import java.util.Optional;

public interface AvatarProvider {
	
	default int avatarWeight() {
		return 0;
	}

	Optional<Avatar> find(AvatarRequest request);
}
