package com.jadaptive.plugins.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
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
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.util.MultiMap;
import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.entity.ObjectNotFoundException;
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
		
	}

	@Override
	public boolean verifyPassword(User user, char[] password) {
		
		File identity4jFile = getConnectorConfiguration();
		
		if(identity4jFile.exists()) {
			
			return executeOnRemoteDatabase(new RemoteDatabaseTask<Boolean>() {
				@Override
				public Boolean executeOnRemote() {
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

	@Override
	public User getUser(String username) {
		
		File identity4jFile = getConnectorConfiguration();
		
		if(identity4jFile.exists()) {
			
			return executeOnRemoteDatabase(new RemoteDatabaseTask<User>() {

				@Override
				public User executeOnRemote() {
					try {
						Connector<?> con = createConnector(identity4jFile);
						return new RemoteUser(con.getIdentityByName(username));
						} catch(PrincipalNotFoundException e) {
							throw new ObjectNotFoundException("Remote user with username " + username + " not found");
						} catch(ConnectorException e) {
							throw new RepositoryException(e);
						}
				}
			});
		}
		
		throw new ObjectNotFoundException("Remote user with username " + username + " not found");
	}

	@Override
	public User getUserByUUID(String uuid) {
		
		File identity4jFile = getConnectorConfiguration();
		
		if(identity4jFile.exists()) {
			
			return executeOnRemoteDatabase(new RemoteDatabaseTask<User>() {

				@Override
				public User executeOnRemote() {
					try {
						Connector<?> con = createConnector(identity4jFile);
						return new RemoteUser(con.getIdentityByGuid(uuid));
						} catch(PrincipalNotFoundException e) {
							throw new ObjectNotFoundException("Remote user with guid " + uuid + " not found");
						} catch(ConnectorException e) {
							throw new RepositoryException(e);
						}
				}
			});
		}
		
		throw new ObjectNotFoundException("Remote user with uuid " + uuid + " not found");
	}

	@Override
	public Iterable<User> iterateUsers() {

		File identity4jFile = getConnectorConfiguration();
		
		if(identity4jFile.exists()) {
			return new ConnectorIterable(createConnector(identity4jFile), this);
		} else {
			return Collections.emptyList();
		}
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
	public Class<? extends User> getUserClass() {
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
}
