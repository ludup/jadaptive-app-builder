package com.jadaptive.app.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.jadaptive.api.app.App;
import com.jadaptive.api.cluster.BroadcastableEvent;
import com.jadaptive.api.cluster.ClusterEvent;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.tenant.Tenant;
import com.sshtools.gardensched.ClusterID;
import com.sshtools.gardensched.DistributedTask;
import com.sshtools.gardensched.ObjectStore;
import com.sshtools.gardensched.PayloadFilter;
import com.sshtools.gardensched.PayloadSerializer;
import com.sshtools.gardensched.SerializableCallable;
import com.sshtools.gardensched.SerializableRunnable;
import com.sshtools.gardensched.TaskCompletionContext;
import com.sshtools.gardensched.TaskErrorHandler;
import com.sshtools.gardensched.TaskSpec;
import com.sshtools.gardensched.TaskSuccessHandler;
import com.sshtools.gardensched.spring.CronTriggerSerializer;
import com.sshtools.gardensched.spring.JsonPayloadSerializer;
import com.sshtools.gardensched.spring.PeriodicTriggerSerializer;
import com.sshtools.gardensched.spring.TriggerAdapter;

@Configuration
public class SchedulerSpringConfig implements TaskErrorHandler, TaskSuccessHandler {
	
	@Autowired
	private ClassLoaderService classLoader;
	
	@Autowired
	private App  appService;
	
	@Bean
	public ObjectStore objectStorage() {
		return new SchedulerObjectStorage();
	}

	@Bean
	public PayloadSerializer taskSerializer() {
		return new JsonPayloadSerializer(createObjectMapper(), appService::resolveClass);
	}

	@Bean
	public PayloadFilter taskFilter(ApplicationContext context) {
		return new PayloadFilter() {
			
			@Override
			public <O> O filter(O task) {
				return task == null ? null : appService.autowire(task);
			}
		};
	}

	@Override
	public void handleError(ClusterID id, TaskSpec spec, DistributedTask<?> task, TaskCompletionContext context,
			Throwable exception) {
		appService.getBean(SchedulerService.class).handleError(id, spec, task, context, exception);
	}

	@Override
	public void handleSuccess(ClusterID id, TaskSpec spec, DistributedTask<?> task, TaskCompletionContext context) {
		appService.getBean(SchedulerService.class).handleSuccess(id, spec, task, context);
	}
	
	private ObjectMapper createObjectMapper() {
		var sm = new SimpleModule();
		sm.addSerializer(CronTrigger.class, new CronTriggerSerializer());
		sm.addSerializer(PeriodicTrigger.class, new PeriodicTriggerSerializer());

		var ptv = BasicPolymorphicTypeValidator.builder().
				allowIfSubType(TriggerAdapter.class).
				allowIfSubType(TenantJobRunner.class).
				allowIfSubType(CronTrigger.class).
				allowIfSubType(PeriodicTrigger.class).
				allowIfSubType(ClusterEvent.class).
				allowIfSubType(Date.class).
				allowIfSubType(Tenant.class).
				allowIfSubType(ArrayList.class).
				allowIfSubType(HashSet.class).
				allowIfSubType(HashMap.class).
				allowIfSubType(LinkedHashSet.class).
				allowIfBaseType(Enum.class).
				allowIfBaseType(ObjectEvent.class).
				allowIfBaseType(SystemEvent.class).
				allowIfBaseType(UUIDEntity.class).
				allowIfBaseType(BroadcastableEvent.class).
				allowIfBaseType(TenantTask.class).
				allowIfBaseType(DistributedTask.class).
				allowIfBaseType(SerializableRunnable.class).
				allowIfBaseType(SerializableCallable.class).
				/* TODO below is very liberal, possibly too liberal and negates most of the above,
				 * but it was needed at the time. revisit
				 */
				allowIfBaseType(Serializable.class).
				build();
				
		var om = JsonMapper.builder()
			    .addModule(new Jdk8Module())
			    .addModule(sm)
			    .configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true)
			    .activateDefaultTyping(ptv, 
			    		ObjectMapper.DefaultTyping.NON_FINAL_AND_ENUMS,
			    		JsonTypeInfo.As.WRAPPER_ARRAY)
				.visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY)
				.visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
				.visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
				.visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)

			    .build();
		
		om.setTypeFactory(om.getTypeFactory().withClassLoader((ClassLoader)classLoader));
		return om;
	}
}
