package com.jadaptive.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.TenantService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { MongoDatabaseService.class, TenantService.class })
@EnableConfigurationProperties
public class DatabaseTests {

	@Autowired
	MongoDatabaseService mdb;
	
	@Autowired
	TenantService tenantService; 
	
	@Test
	public void testSystemTenant() {
		
		try {
			tenantService.getSystemTenant();
		} catch (RepositoryException | EntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
//	@BeforeClass
//	public static void setupDatabase() throws RepositoryException, EntityException {
//		
//		
//		TenantRepositoryImpl mdb = new TenantRepositoryImpl();
//		mdb.newSchema();
//		
//		mdb.saveTenant(new Tenant("ea64d65f-60e3-4a1f-b7b3-5f4a3911c97f", "JADAPTIVE", "app.jadaptive.com"));
//
//		for(Tenant tenant : mdb.listTenants()) {
//			System.out.println(tenant.getName());
//			System.out.println(tenant.getHostname());
//			System.out.println(tenant.getUuid());
//		}
//		
//		EntityTemplate t = new EntityTemplate();
//		t.setName("Test Entity");
//		t.setType(EntityType.SINGLETON);
//		t.setUuid("c1af2043-cb1b-433e-a177-afd3fd811faa");
//
//		Set<FieldTemplate> fields = new HashSet<>();
//		Set<FieldValidator> validators = new HashSet<>();
//		FieldValidator v = new FieldValidator();
//		v.setType(ValidationType.LENGTH);
//		v.setValue("255");
//		v.setUuid("47828615-14b3-41f9-9e05-28225dfacf99");
//		
//		validators.add(v);
//		
//		FieldTemplate f = new FieldTemplate();
//		f.setDefaultValue("Its the Data");
//		f.setDescription("Some description of the field");
//		f.setFieldType(FieldType.TEXT);
//		f.setResourceKey("field1");
//		f.setUuid("128661fe-3f9e-4859-97fc-9c01f0556513");
//		f.setWeight(0);
//		
//		f.setValidators(validators);
//		
//		fields.add(f);
//		
//		t.setFields(fields);
//
//		mdb.saveObject(t, "tenant1");
//		
//		EntityTemplate t2 = mdb.getObject("c1af2043-cb1b-433e-a177-afd3fd811faa", "tenant1", EntityTemplate.class);
//		
//		System.out.println();;
//		
//		
//	}
}
