package com.jadaptive.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.db.mock.MockDocumentDatabaseImpl;

public class TenantRepositoryTests {

	private DocumentDatabase db = new MockDocumentDatabaseImpl();
	
	private TenantRepository tenantRepository;
	
	@Before
	public void initTest() {
		tenantRepository = new TenantRepositoryImpl(db);
		tenantRepository.newSchema();		
	}
	
	@Test
	public void verifyNewSchema() {
		
		assertEquals(tenantRepository.countTenants(), new Long(1L));
	}
}
