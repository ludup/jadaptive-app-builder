package com.jadaptive.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.db.mock.MockDocumentDatabaseImpl;
import com.jadaptive.entity.EntityException;

public class TenantRepositoryTests {

	private DocumentDatabase db = new MockDocumentDatabaseImpl();
	
	private TenantRepository tenantRepository;
	
	@Before
	public void initTest() {
		tenantRepository = new TenantRepositoryImpl(db);
		tenantRepository.newSchema();		
	}
	
	@Test
	public void testNewSchemaTenantCount() {
		assertEquals(tenantRepository.countTenants(), new Long(1L));
	}
	
	@Test
	public void testNewSchemaSystemTenantExists() {
		assertNotNull(tenantRepository.getSystemTenant());
		assertEquals(tenantRepository.getSystemTenant().getUuid(), TenantRepository.SYSTEM_UUID);
	}
	
	@Test
	public void testNewTenantNoUUID() {
		
		Tenant tenant = new Tenant();
		tenant.setName("No UUID");
		tenant.setHostname("test.jadaptive.com");
		
		assertNull(tenant.getUuid());
		
		tenantRepository.saveTenant(tenant);
		
		assertNotNull(tenant.getUuid());
	}
	
	@Test
	public void testNewTenantWithUUID() {
		
		Tenant tenant = new Tenant();
		tenant.setName("No UUID");
		tenant.setHostname("test.jadaptive.com");
		
		String uuid = UUID.randomUUID().toString();
		
		tenant.setUuid(uuid);
		
		tenantRepository.saveTenant(tenant);
		
		assertNotNull(tenant.getUuid());
		assertEquals(tenant.getUuid(), uuid);
	}
	
	@Test(expected=EntityException.class)
	public void testCannotDeleteSystemTenant() {
		
		tenantRepository.deleteTenant(tenantRepository.getSystemTenant());
		
	}
}
