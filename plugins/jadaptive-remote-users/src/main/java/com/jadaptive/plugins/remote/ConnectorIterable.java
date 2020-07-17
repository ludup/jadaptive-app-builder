package com.jadaptive.plugins.remote;

import java.util.Iterator;

import com.identity4j.connector.Connector;
import com.identity4j.connector.principal.Identity;
import com.jadaptive.api.user.User;

public class ConnectorIterable implements Iterable<User> {

	Connector<?> con;
	RemoteUserDatabase database;
	public ConnectorIterable(Connector<?> con, RemoteUserDatabase database) {
		this.con = con;
		this.database = database;
	}
	
	@Override
	public Iterator<User> iterator() {
		return database.executeOnRemoteDatabase(new RemoteDatabaseTask< Iterator<User>>() {
			@Override
			public Iterator<User> executeOnRemote() {
				return new ConnectorIterator(con.allIdentities());
			}
		});
	}
	
	class ConnectorIterator implements Iterator<User> {

		Iterator<Identity> identities;
		
		ConnectorIterator(Iterator<Identity> identities) {
			this.identities = identities;
		}
		
		@Override
		public boolean hasNext() {
			return database.executeOnRemoteDatabase(new RemoteDatabaseTask<Boolean>() {

				@Override
				public Boolean executeOnRemote() {
					return identities.hasNext();
				}
			});
		}

		@Override
		public User next() {
			return database.executeOnRemoteDatabase(new RemoteDatabaseTask<User>() {

				@Override
				public User executeOnRemote() {
					return convertUser(identities.next());
				}
			});
		}

		private User convertUser(Identity identity) {
			return new RemoteUser(identity);
		}
		
	}

}
