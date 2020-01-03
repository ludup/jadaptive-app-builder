package com.jadaptive.app.session;

import org.springframework.stereotype.Repository;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionRepository;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.tenant.AbstractTenantAwareObjectDatabaseImpl;

@Repository
public class SessionRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<Session> implements SessionRepository {

	protected SessionRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public Class<Session> getResourceClass() {
		return Session.class;
	}


}
