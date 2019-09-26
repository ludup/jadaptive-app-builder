package com.jadaptive.session;

import org.springframework.stereotype.Repository;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.tenant.AbstractTenantAwareObjectDatabaseImpl;

@Repository
public class SessionRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<Session> implements SessionRepository {

	protected SessionRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	protected Class<Session> getResourceClass() {
		return Session.class;
	}


}
