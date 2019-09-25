package com.jadaptive.role;

import org.springframework.stereotype.Repository;

import com.jadaptive.db.AbstractObjectDatabaseImpl;
import com.jadaptive.db.DocumentDatabase;

@Repository
public class RoleRepositoryImpl extends AbstractObjectDatabaseImpl implements RoleRepository {

	protected RoleRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

}
