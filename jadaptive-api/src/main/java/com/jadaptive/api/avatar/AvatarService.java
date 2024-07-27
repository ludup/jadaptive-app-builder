package com.jadaptive.api.avatar;

import java.util.List;

public interface AvatarService {

	List<Avatar> avatars(AvatarRequest request);
	
	Avatar avatar(AvatarRequest request);
}
