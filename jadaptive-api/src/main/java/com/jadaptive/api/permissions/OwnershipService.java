package com.jadaptive.api.permissions;

import com.jadaptive.api.repository.UUIDDocument;

public interface OwnershipService {

	<T extends UUIDDocument> boolean isOwner(T obj);
}
