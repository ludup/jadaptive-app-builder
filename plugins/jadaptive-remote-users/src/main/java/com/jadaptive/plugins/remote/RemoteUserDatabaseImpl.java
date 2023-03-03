package com.jadaptive.plugins.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.MultiMap;
import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserDatabaseCapabilities;

@Extension
public class RemoteUserDatabaseImpl extends AuthenticatedService implements RemoteUserDatabase {
	
	static Logger log = LoggerFactory.getLogger(RemoteUserDatabaseImpl.class);
	
	@Autowired
	private ClassLoaderService classService; 
	
	private Map<String,Connector<?>> cachedConnectors = new HashMap<>();
	
	@Override
	public <T> T executeOnRemoteDatabase(RemoteDatabaseTask<T> task) {
		
		ClassLoader prev = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classService.getClassLoader());
		
		try {
			return task.executeOnRemote();
		} finally {
			Thread.currentThread().setContextClassLoader(prev);
		}
	}
	
	@Override
	public void setPassword(User user, char[] password, boolean passwordChangeRequired) {
		
		File identity4jFile = getConnectorConfiguration();
		
		if(identity4jFile.exists()) {
				executeOnRemoteDatabase(new RemoteDatabaseTask<Void>() {
					@Override
					public Void executeOnRemote() {
						
						if(log.isInfoEnabled()) {
							log.info("Remote user {} password change", user.getUsername());
						}
						
						Connector<?> con = createConnector(identity4jFile);
						
						try {
							con.setPassword(user.getUsername(), user.getUuid(), password, passwordChangeRequired);
							return null;
						} catch(ConnectorException e) {
							log.error("Failed to change password", e);
							throw new RepositoryException(e);
						}
					}
					
				});
		}
	}

	@Override
	public boolean verifyPassword(User user, char[] password) {
		
		File identity4jFile = getConnectorConfiguration();
		
		if(identity4jFile.exists()) {
			
			return executeOnRemoteDatabase(new RemoteDatabaseTask<Boolean>() {
				@Override
				public Boolean executeOnRemote() {
					
					if(log.isInfoEnabled()) {
						log.info("Remote user {} verify password", user.getUsername());
					}
					
					Connector<?> con = createConnector(identity4jFile);
					
					try {
						con.logon(user.getUsername(), password);
						return true;
					} catch(ConnectorException e) {
						log.error("Bad login", e);
						return false;
					}
				}
				
			});
		}
		
		return false;
	}


	private File getConnectorConfiguration() {
		Tenant tenant = getCurrentTenant();
		File confFolder;
		if(tenant.isSystem()) {
			confFolder = ConfigHelper.getSystemFolder();
		} else {
			confFolder = ConfigHelper.getTenantFolder(tenant);
		}
		
		return new File(confFolder, "identity4j.properties");
	}
	
	private Connector<?> createConnector(File identity4jFile) {
		
		Connector<?> con = cachedConnectors.get(getCurrentTenant().getUuid());
		if(Objects.nonNull(con) && con.isOpen()) {
			return con;
		}
		
		return executeOnRemoteDatabase(new RemoteDatabaseTask<Connector<?>>() {

			@Override
			public Connector<?> executeOnRemote() {
				
				
				if(log.isInfoEnabled()) {
					log.info("Creating a remote user connector for tenant {}", getCurrentTenant().getName());
				}
				
				Properties properties = new Properties();
				try(InputStream in = new FileInputStream(identity4jFile)) {
					properties.load(in);
					Connector<?> con = createConnectorFromProperties(properties);
					cachedConnectors.put(getCurrentTenant().getUuid(), con);
					return con;
				} catch (IOException e) {
					throw new RepositoryException(e);
				} 
			}
		});
	}

	@Override
	public ObjectTemplate getUserTemplate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDatabaseUser(User user) {
		return user instanceof RemoteUser;
	}

	@Override
	public Class<RemoteUser> getUserClass() {
		return RemoteUser.class;
	}

	@Override
	public Set<UserDatabaseCapabilities> getCapabilities() {
		return new HashSet<>(Arrays.asList(UserDatabaseCapabilities.LOGON, UserDatabaseCapabilities.MODIFY_PASSWORD));
	}

	@Override
	public void deleteUser(User user) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateUser(User user) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createUser(User user, char[] password, boolean forceChange) {
		throw new UnsupportedOperationException();
	}
	
	public final Connector<?> createConnectorFromProperties(Properties properties) throws IOException {
		MultiMap params = MultiMap.toMultiMap(properties);
		ConnectorBuilder bui = new ConnectorBuilder();
		Connector<?> connector = bui.buildConnector(params);
		if (!connector.isOpen()) {
			throw new IOException("Connector configuration is not connect");
		}
		return connector;
	}
	
	@Override
	public Integer weight() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean hasEncryptedPassword(User u) {
		return true;
	};
}
