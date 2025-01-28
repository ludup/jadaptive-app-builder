package com.jadaptive.plugins.keys;

import java.io.IOException;
import java.util.Collection;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.user.User;
import com.sshtools.common.ssh.components.SshPublicKey;

public interface AuthorizedKeyDatabase extends ExtensionPoint {

	Collection<SshPublicKey> getPublicKeys(User user) throws IOException;
}
