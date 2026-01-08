package com.jadaptive.app.scheduler;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.SchedulerTask;
import com.jadaptive.api.scheduler.SchedulerTask.SchedulerTaskType;
import com.jadaptive.api.tenant.TenantService;
import com.sshtools.gardensched.ClusterID;
import com.sshtools.gardensched.DistributedCallable;
import com.sshtools.gardensched.DistributedRunnable;
import com.sshtools.gardensched.DistributedTask;
import com.sshtools.gardensched.PayloadSerializer;
import com.sshtools.gardensched.SerializableCallable;
import com.sshtools.gardensched.SerializableRunnable;
import com.sshtools.gardensched.TaskEntry;
import com.sshtools.gardensched.TaskSpec;
import com.sshtools.gardensched.TaskStore;
import com.sshtools.gardensched.TaskTrigger;

@Component
public class SchedulerTaskStorage implements TaskStore {
	
	final static Logger LOG = LoggerFactory.getLogger(SchedulerTaskStorage.class);
	
	@Autowired
	private SchedulerService schedulerService;
	
	@Autowired
	private TenantService tenantService;
	
	@Autowired
	private PayloadSerializer payloadSerializer;

	@Override
	public void store(TaskEntry entry) {
		tenantService.asSystem(() -> {
			tenantService.executeAs(tenantService.getTenantByUUID(entry.task().classifiers().iterator().next()), () -> {
				
				var schedulerTask = new SchedulerTask();
				
				schedulerTask.setUuid(toUuid(entry.id()));
				schedulerTask.setId(entry.id().toString());
				schedulerTask.setSubmitter(entry.submitter().toString());
				
				schedulerTask.setAffinity(entry.task().affinity());
				schedulerTask.setClassifiers(entry.task().classifiers().stream().toList());
				schedulerTask.setConflictResolution(entry.task().onConflict());
				
				schedulerTask.setSchedule(entry.spec().schedule());
				schedulerTask.setInitialDelay(entry.spec().initialDelay());
				schedulerTask.setPeriod(entry.spec().period());
				schedulerTask.setTimeUnit(entry.spec().unit());
				schedulerTask.setSubmitted(new Date(entry.spec().submitted().toEpochMilli()));
				schedulerTask.setTaskType(entry.task() instanceof DistributedRunnable ? SchedulerTaskType.RUNNABLE : SchedulerTaskType.CALLABLE);
				
				schedulerTask.setKey(entry.task().key());
				schedulerTask.setBundle(entry.task().bundle().orElse(null));
				schedulerTask.setName(entry.task().name().orElse(null));
				schedulerTask.setDisplayName(entry.task().displayName());
				
				
				try {
					var attrs = entry.task().attributes();
					for(var attrEntry : attrs.entrySet()) {
						schedulerTask.getAttributes().add(attrEntry.getKey() + "=" +  payloadSerializer.serializeToString(attrEntry.getValue()));
					}
					
					schedulerTask.setTrigger(payloadSerializer.serializeToString(entry.spec().trigger()));
					schedulerTask.setTask(payloadSerializer.serializeToString(entry.task().task()));
				}
				catch(IOException uioe) {
					throw new UncheckedIOException(uioe);
				}
				
				schedulerService.saveOrUpdate(schedulerTask);
			});	
		});
		
	}

	@Override
	public Stream<TaskEntry> entries() {
		var l = new  ArrayList<TaskEntry>();
		tenantService.asSystem(() -> {
			for(var tenant : tenantService.allObjects()) {
				LOG.info("Gathering tasks for tenant {} ({})", tenant.getUuid(), tenant.getName());
				tenantService.executeAs(tenant, () -> {
					l.addAll(schedulerService.streamAll().map(st -> {
						return schedulerTaskToEntry(st);
					}).toList());
				});
			}
		});
		return l.stream();
	}

	@Override
	public void remove(ClusterID id, Set<String> classifiers) {
		tenantService.executeAs(tenantService.getTenantByUUID(classifiers.iterator().next()), () -> {
			schedulerService.deleteObjectByUUID(toUuid(id));
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TaskEntry schedulerTaskToEntry(SchedulerTask tsk) {
		try {
			var job = payloadSerializer.deserializeFromString(Serializable.class, tsk.getTask());
			var trigger = (TaskTrigger)payloadSerializer.deserializeFromString(TaskTrigger.class, tsk.getTrigger());
			var spec = new TaskSpec(tsk.getSubmitted().toInstant(), tsk.getSchedule(), tsk.getInitialDelay(), tsk.getPeriod(), tsk.getTimeUnit(), trigger);
			
			/* TODO is a string list (name=value) our best choice here (Map appears not supported by entity system? check with ludup */
			var attrs = tsk.getAttributes().stream().
					map(e -> {
						try {
							var idx = e.indexOf('=');
							var key = e;
							Serializable val = null;
							if(idx > -1) {
								key = e.substring(0, idx);
								val = (Serializable)payloadSerializer.deserializeFromString(Serializable.class, e.substring(idx + 1));
							}
							return new AbstractMap.SimpleEntry<String, Serializable>(key, val);
						} catch (IOException e1) {
							throw new UncheckedIOException(e1);
						}
					}).
					collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			DistributedTask<?> ntsk;
			
			if(tsk.getTaskType() == SchedulerTaskType.RUNNABLE) {
				ntsk = new DistributedRunnable.Builder(tsk.getId(), (SerializableRunnable) job).
						withAffinity(tsk.getAffinity()).
						withName(tsk.getName()).
						withKey(tsk.getKey()).
						withAttributes(attrs).
						withBundle(tsk.getBundle()).
						withPersistent(false).
						build();
			}
			else {
				ntsk = new DistributedCallable.Builder<>(tsk.getId(), (SerializableCallable) job).
						withAffinity(tsk.getAffinity()).
						withName(tsk.getName()).
						withKey(tsk.getKey()).
						withAttributes(attrs).
						withBundle(tsk.getBundle()).
						withPersistent(false).
						build();
			}

			return new TaskEntry(
				ClusterID.parse(tsk.getId()), 
				ntsk, 
				tsk.getSubmitter(), 
				spec
			);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	static String toUuid(ClusterID id) {
		if(id.getStrId() != null) {
			try {
				return UUID.fromString(id.getStrId()).toString();
			}
			catch(IllegalArgumentException iae) {
			}
		}

		try {
			return UUID.nameUUIDFromBytes(id.toString().getBytes("UTF-8")).toString();
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

}
