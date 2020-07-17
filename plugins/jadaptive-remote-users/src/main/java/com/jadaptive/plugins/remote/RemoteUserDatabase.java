package com.jadaptive.plugins.remote;

import com.jadaptive.api.user.UserDatabase;

public interface RemoteUserDatabase extends UserDatabase {

	<T> T executeOnRemoteDatabase(RemoteDatabaseTask<T> task);

}
