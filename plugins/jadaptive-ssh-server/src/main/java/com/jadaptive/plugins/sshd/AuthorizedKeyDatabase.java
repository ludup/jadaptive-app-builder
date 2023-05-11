package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.util.Collection;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.user.User;
import com.sshtools.common.ssh.components.SshPublicKey;

public interface AuthorizedKeyDatabase extends ExtensionPoint {

	Collection<SshPublicKey> getAuthorizedKeys(User user) throws IOException;
}
