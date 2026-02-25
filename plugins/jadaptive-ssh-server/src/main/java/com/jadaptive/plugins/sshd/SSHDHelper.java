package com.jadaptive.plugins.sshd;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.App;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.PermissionService.UncheckedCloseable;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.ssh.SshConnection;

public abstract class SSHDHelper {
	
	public interface UserSpecContext extends UncheckedCloseable {
		Optional<User> user();
		Optional<Tenant> tenant();
		UserSpec spec();
	}

	@Autowired
	protected TenantService tenantService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	protected UserService userService;

	@Autowired
	protected App applicationService;

	public final UserSpecContext tryAs(SshConnection con) {
		return tryAs(userSpec(con));
	}

	public final UserSpecContext tryAs(UserSpec spec) {
		var c1 = tryAsTenant(spec);
		try {
			var c2 = tryAsUser(spec);
			return new UserSpecContext() {
				@Override
				public void close() {
					try {
						c2.close();
					}
					finally {
						c1.close();
					}
				}

				@Override
				public Optional<User> user() {
					return c2.user();
				}

				@Override
				public Optional<Tenant> tenant() {
					return c1.tenant();
				}

				@Override
				public UserSpec spec() {
					return spec;
				}
			};
		}
		catch(RuntimeException re) {
			try {
				c1.close();
			}
			catch(Exception e) {}
			throw re;
		}
		
	}
	
	public final UserSpecContext tryAsUser(SshConnection con) {
		return tryAsUser(userSpec(con));
	}
	
	public final UserSpecContext tryAsUser(UserSpec spec) {
		var usr = userService.getUser(spec.username());
		var uctx = permissionService.userContext(usr);
		return new UserSpecContext() {
			
			@Override
			public void close() {
				uctx.close();
			}
			
			@Override
			public Optional<User> user() {
				return Optional.of(usr);
			}
			
			@Override
			public Optional<Tenant> tenant() {
				return Optional.empty();
			}
			
			@Override
			public UserSpec spec() {
				return spec;
			}
		};
	}
	
	public final UserSpecContext tryAsTenant(SshConnection con) {
		return tryAsTenant(userSpec(con));
	}
	
	public final UserSpecContext tryAsTenant(UserSpec spec) {
		if(spec.tenantOr().isPresent()) {
			var ten = getTenant(spec);
			var tc = tenantService.tenant(ten);
			return new UserSpecContext() {
				
				@Override
				public void close() {
					tc.close();
				}
				
				@Override
				public Optional<User> user() {
					return Optional.empty();
				}
				
				@Override
				public Optional<Tenant> tenant() {
					return Optional.of(ten);
				}
				
				@Override
				public UserSpec spec() {
					return spec;
				}
			};
		}
		else
			return new UserSpecContext() {
				@Override
				public void close() { }

				@Override
				public Optional<User> user() {
					return Optional.empty();
				}

				@Override
				public Optional<Tenant> tenant() {
					return Optional.empty();
				}

				@Override
				public UserSpec spec() {
					return spec;
				}
			};
	}
	
	public final UserSpecContext tryAsTenantOrSystem(SshConnection con) {
		return tryAsTenantOrSystem(userSpec(con));
	}
	
	public final UserSpecContext tryAsTenantOrSystem(UserSpec spec) {
		UncheckedCloseable uc;
		if(spec.tenantOr().isPresent()) {
			uc = tenantService.tenant(getTenant(spec));
		}
		else
			uc = tenantService.systemTenant();
		
		var ten = tenantService.getCurrentTenant();
		
		return new UserSpecContext() {
			@Override
			public void close() {
				uc.close();
			}
			
			@Override
			public Optional<User> user() {
				return Optional.empty();
			}
			
			@Override
			public Optional<Tenant> tenant() {
				return Optional.of(ten);
			}
			
			@Override
			public UserSpec spec() {
				return spec;
			}
		};
	}

	public final UserSpec userSpec(SshConnection con) {
		return userSpec(con, con.getUsername());
	}

	public final UserSpec userSpec(SshConnection con, String userspec) {
		return applicationService.getBean(getInterface(con).getInterfaceFactory()).userSpec(userspec);
	}

	public abstract SSHInterface getInterface(SshConnection con);


	protected Tenant getTenant(UserSpec spec) {
		try {
			return tenantService.getTenantByDomain(spec.tenant());
		}
		catch(ObjectNotFoundException onfe) {
			return tenantService.getTenantByUUID(spec.tenant());
		}
	}
}
