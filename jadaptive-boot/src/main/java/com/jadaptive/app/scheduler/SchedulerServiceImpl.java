package com.jadaptive.app.scheduler;

import static com.jadaptive.app.scheduler.Jobs.formatDisplayDuration;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.cluster.ClusterManager;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.ScheduledTaskConfig;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.SchedulerTask;
import com.jadaptive.api.scheduler.SchedulerTask.SchedulerTaskStatus;
import com.jadaptive.api.scheduler.SchedulerTaskCompleteEvent;
import com.jadaptive.api.scheduler.TenantTask;
import com.jadaptive.api.scheduler.TenantTaskConfig;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;
import com.sshtools.gardensched.ClusterID;
import com.sshtools.gardensched.ConflictResolution;
import com.sshtools.gardensched.DistributedRunnable;
import com.sshtools.gardensched.DistributedRunnable.Builder;
import com.sshtools.gardensched.DistributedScheduledExecutor;
import com.sshtools.gardensched.DistributedTask;
import com.sshtools.gardensched.IdentifiableFuture;
import com.sshtools.gardensched.IdentifiableScheduledFuture;
import com.sshtools.gardensched.ObjectStore;
import com.sshtools.gardensched.PayloadFilter;
import com.sshtools.gardensched.PayloadSerializer;
import com.sshtools.gardensched.TaskCompletionContext;
import com.sshtools.gardensched.TaskInfo;
import com.sshtools.gardensched.TaskSpec;
import com.sshtools.gardensched.TaskStore;
import com.sshtools.gardensched.spring.GardenSchedTaskScheduler;

@Service
public class SchedulerServiceImpl extends AbstractUUIDObjectServceImpl<SchedulerTask> implements SchedulerService, TenantAware, StartupAware, Lifecycle, ObjectStore {


	private static Logger LOG = LoggerFactory.getLogger(SchedulerServiceImpl.class);
	
	private DistributedScheduledExecutor executor;	
	
	@Autowired
	private App applicationService; 
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private I18nService i18nService;
	
	@Autowired
	private TaskStore taskStore;
	
	@Autowired
	private ObjectStore objectStore;

	@Autowired
	private PayloadSerializer taskSerializer;

	@Autowired
	private PayloadFilter taskFilter;

	@Autowired
	private ClusterManager clusterManager;

	private GardenSchedTaskScheduler taskScheduler;

	@Override
	public void initializeSystem(boolean newSchema) {
		
		/* We need a very particular start-up  order here.
		 * 
		 * 1. Firstly the MongoDB database collection of ClusterNode must be updated
		 *    with as much information as we can initially gather.
		 *    
		 * 2. We start the distributed scheduler. If there are nodes on available in the cluster,
		 *    we will authenticate with them. Encryption / Decryption services must be available at
		 *    this point if that happens.
		 *    
		 * 3. We complete the cluster setup by setting up the event proxy and updating the final
		 *    status and the nodes group name.
		 *    
		 * 4. Complete other initialization of system.
		 */
		
		clusterManager.initCluster();
		
		var poolThreads = ApplicationProperties.getValue("ha.poolThreads", Runtime.getRuntime().availableProcessors());
		LOG.info("Schedule Threads: {}", poolThreads);
		
		var bldr = new DistributedScheduledExecutor.Builder().
				withPayloadSerializer(taskSerializer).
				withPayloadFilter(taskFilter).
				withTaskStore(taskStore).
				withTaskErrorHandler(this).
				withTaskSuccessHandler(this).
				withDeferStorageUntilStarted().
				withPersistentByDefault().
				withStartPaused().
                withAlwaysDistribute().
				withObjectStore(objectStore).
				withCloseTimeout(Duration.ofMinutes(ApplicationProperties.getValue("ha.shutdownTimeout", Integer.MAX_VALUE))).
				withSchedulerThreads(
					poolThreads
				);
		
		var haProps = ApplicationProperties.getValue("ha.props", SchedulerService.JAD_JGROUPS);
		bldr.withJGroupsProps(haProps);
		LOG.info("JGroups Properties: {}", haProps);
		
		var haClusterName = ApplicationProperties.getValue("ha.clusterName", clusterManager.getServerId());
			bldr.withClusterName(haClusterName);
			LOG.info("Cluster Name: {}", haClusterName);
		
		var haGroupName = ApplicationProperties.getValue("ha.groupName", "");
		if(!haGroupName.equals("")) {
			bldr.withGroupName(haGroupName);
		}
		
		try {
			executor = bldr.build();
		} catch(RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to start distributed scheduler.", e);
		}
		
		taskScheduler = new GardenSchedTaskScheduler(executor);
		
		clusterManager.setupCluster(executor);
		
		initializeTenant(getCurrentTenant(), newSchema);
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Scheduling tasks for {}", tenant.getName());
		}
		
		for(var task  : applicationService.getBeans(ScheduledTask.class)) {
			if(task.isSystemOnly() && !tenant.isSystem()) {
				continue;
			}
			
			var cron = ScheduledTask.AT_MIDNIGHT;
			var annotation = task.getClass().getAnnotation(ScheduledTaskConfig.class);
			if(annotation != null) {
				cron = annotation.value();
				if(annotation.systemOnly() && !tenant.isSystem()) {
					continue;
				}
			}
			
			UUID uuid;
			try {
				uuid = UUID.nameUUIDFromBytes(
					(task.getClass().getName() + "/" + tenant.getUuid()).getBytes("UTF-8"));
			}
			catch(UnsupportedEncodingException uee) {
				throw new IllegalStateException(uee); // as if
			}
			
			var bldr = new DistributedRunnable.Builder(uuid.toString(), new TenantJobRunner(tenant, task)).
				withKey(task.getClass().getName()).
				withClassifiers(tenant.getUuid()).
				fromAnnotatedObject(task);
			
			configureTenantTaskBuilder(bldr, task);
			
			taskScheduler.schedule(
				bldr.build(),
				new CronTrigger(cron)
			);

		}
	}

	@Override
	public void runNow(TenantTask task) {
		var tenant = getCurrentTenant();
		taskScheduler.schedule(new DistributedRunnable.Builder(
				new TenantJobRunner(tenant, task)
			).
			fromAnnotatedObject(task).
			withClassifiers(tenant.getUuid()).
			build(), Instant.now());
	}
	
	@Override
	public void runNow(Runnable task) {
		var tenant = getCurrentTenant();
		taskScheduler.schedule(DistributedRunnable.local(new TenantJobRunner(tenant, () -> task.run()), tenant.getUuid()), Instant.now());
	}
	
	@Override
	public void runAs(User user, Runnable task) {
		taskScheduler.schedule(DistributedRunnable.local(new TenantJobRunner(getCurrentTenant(), () -> {
			task.run();
		},  user)), Instant.now());
	}
	
	@Override
	public void schedule(TenantTask task, String expression, String taskUUID) {
		var tenant = getCurrentTenant();
		taskScheduler.schedule(new DistributedRunnable.Builder(
				taskUUID, 
				new TenantJobRunner(tenant, task)
			).
			fromAnnotatedObject(task).
			withClassifiers(tenant.getUuid()).
			onConflict(ConflictResolution.THROW).
			build(),  new CronTrigger(expression));
		
	}
	
	@Override
	public void schedule(TenantTask task, Date startTime, long repeat, String taskUUID) {
		var tenant = getCurrentTenant();
		taskScheduler.scheduleAtFixedRate(new DistributedRunnable.Builder(
				taskUUID, 
				new TenantJobRunner(tenant, task)
			).
			fromAnnotatedObject(task).
			withClassifiers(tenant.getUuid()).
			onConflict(ConflictResolution.THROW).
			build(),  startTime.toInstant(), Duration.ofMillis(repeat));
		
	}
	
	@Override
	public void schedule(TenantTask task, Date startTime, String taskUUID) {
		var tenant = getCurrentTenant();
		taskScheduler.schedule(new DistributedRunnable.Builder(
				taskUUID, 
				new TenantJobRunner(tenant, task)
			).
			fromAnnotatedObject(task).
			withClassifiers(tenant.getUuid()).
			onConflict(ConflictResolution.THROW).
			build(),  startTime.toInstant());
	}
	
	@Override
	public void cancelTask(String uuid, boolean mayInterrupt) {
		executor.futureOr(ClusterID.parse(uuid)).ifPresentOrElse(ftr -> ftr.cancel(mayInterrupt), () -> LOG.warn("Request to cancel task {} that does not exist.", uuid));
	}

	@Override
	public void runScheduledTaskNow(String uuid) {
		executor.futureOr(ClusterID.parse(uuid)).ifPresentOrElse(ftr -> ftr.runNow(), () -> LOG.warn("Request to run task {} that does not exist.", uuid));
	}

	@Override
	public void onApplicationStartup() {
		
		eventService.deleted(Tenant.class, (evt)-> {
			for(var future : new ArrayList<>(executor.futures())) {
				if(future.classifiers().contains(evt.getObject().getUuid())) {
					/* NOTE: my hunch is it better not to interrupt if this does happen, let it fail if the realm if gone and the task needs it.
					 The task is best place to deal with this as it sees fit.
					*/  
					try {
						future.cancel(false);
					}
					catch(Exception e) {
						if(LOG.isDebugEnabled())
							LOG.warn("Error while cancelling tenants scheduled tasks.", e);
						else
							LOG.warn("Error while cancelling tenants scheduled tasks. {}", e.getMessage());
					}
//					future.cancel(true);
				}
			}
		});
		executor.start();
	}

	@Override
	public void scheduleIn(Runnable task, Duration duration, User user) {
		var tenant = getCurrentTenant();
		taskScheduler.schedule(DistributedRunnable.local(new TenantJobRunner(tenant, () -> {
			task.run();
		}), tenant.getUuid()), Utils.now().toInstant().plus(duration));

	}

	@Override
	public void scheduleIn(Runnable task, Duration duration) {
		scheduleIn(task, duration, null);
	}

	@Override
	public SchedulerTaskStatus getStatus(ClusterID cid) {
		var future = executor.future(cid);
		var info = future == null ? null : future.info();
		return calcStatus(future, info);
	}

	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {

		var tsk = getObjectByUUID(obj.getUuid());
		var cid = ClusterID.parse(tsk.getId());
		var future = executor.future(cid);
		var info = future == null ? null : future.info();
		
		SchedulerTaskStatus status = calcStatus(future, info);
		
		if (column.equals("displayName")) {
			Element nameEl;
			if(StringUtils.isEmpty(tsk.getName())) {
				if(StringUtils.isEmpty(tsk.getKey()) || StringUtils.isEmpty(tsk.getBundle()))
					nameEl = Html.span(tsk.getDisplayName());
				else 
					nameEl = Html.i18n(tsk.getBundle(), tsk.getKey() + ".name");
			}
			else {
				nameEl = Html.span(tsk.getName());
			}
			
			var div1 = Html.div();
			var div2 = Html.div();
			div1.appendChild(nameEl);
			
			if(tsk.getKey() != null && tsk.getBundle() != null) {
				var descSpan = Html.i18n(tsk.getBundle(), tsk.getKey() + ".desc");
				descSpan.addClass("text-muted");
				div2.appendChild(descSpan);
			}
			
			var odiv = Html.div();
			odiv.appendChild(div1);
			odiv.appendChild(div2);
			if(info != null) {
				
				if(info.progress().isPresent()) {
					var div3 = Html.div("progress", "auto-progress-bar").
							attr("role", "progressbar").
							attr("aria-label", "Progress of task " + nameEl.text()).
							attr("aria-valuenow", String.valueOf(info.progress().orElse(0l))).
							attr("aria-valuemin", "0").
							attr("aria-valuemax", String.valueOf(info.maxProgress().orElse(100l)));
					
					var div3i = Html.div("progress-bar", "bg-success");
					if(info.message().isPresent()) {
						div3i.text(info.message().get());
					}
					else if(info.key().isPresent()  && info.bundle().isPresent()) {
						div3i.text(i18nService.format(info.bundle().get(), Locale.getDefault(), info.key().get(), (Object[])info.args().orElseGet(() -> new String[0])));
					}
					div3.appendChild(div3i);	
					odiv.appendChild(div3);
				} else {

					var endTime = info.lastCompleted().map(lc -> 
						DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(lc.atZone(ZoneId.systemDefault()))).
						orElse(null);
					var taken = info.taken();
					
					if(info.lastError().isPresent()) {
						var errDiv = new Element("em");
						errDiv.addClass("text-muted");
						taken.ifPresentOrElse(tkn -> {
							errDiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, 
								"displayName.failedAtTaken", 
								info.lastError().get().getMessage(),
								endTime,
								Jobs.formatDisplayDuration(tkn)));
						}, () -> {
							errDiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, 
								info.lastCompleted().isPresent() 
									? "displayName.failedAt" 
									: "displayName.failed",
								info.lastError().get().getMessage(),
								endTime));
						});
						
						odiv.appendChild(errDiv);
					}
					else if(info.lastCompleted().isPresent()) {
						
						var errDiv = new Element("em");
						errDiv.addClass("text-muted");
						
						taken.ifPresentOrElse(tkn -> {
							errDiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, 
								"displayName.completedAtTaken", endTime, Jobs.formatDisplayDuration(tkn)));
						}, () -> {
							errDiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, 
								"displayName.completedAt", endTime));
						});
						odiv.appendChild(errDiv);
					}
				}
			}
			return odiv;
		}
		else if (column.equals("details")) {

			var odiv = Html.div();
			
			switch(tsk.getSchedule()) {
			case TRIGGER:
				odiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "details.trigger", info.spec().trigger().toString()));
				break;
			case NOW:
				odiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "details.now"));
				break;
			case FIXED_DELAY:
				odiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "details.fixedDelay", formatDisplayDuration(Duration.ofMillis(info.spec().initialDelay())), formatDisplayDuration(Duration.ofMillis(info.spec().period()))));
				break;
			case FIXED_RATE:
				odiv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "details.fixedDelay", formatDisplayDuration(Duration.ofMillis(info.spec().initialDelay())), formatDisplayDuration(Duration.ofMillis(info.spec().period()))));
				break;
			case ONE_SHOT:
				break;
			}
			
			if(future instanceof IdentifiableScheduledFuture isf) {
				var ediv = Html.div("text-muted");
				var dur = Duration.ofMillis(isf.getDelay(TimeUnit.MILLISECONDS));
				var et = Instant.ofEpochMilli(Instant.now().toEpochMilli() + dur.toMillis());
				ediv.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "details.remainBeforeTask", 
						formatDisplayDuration(dur),
						DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(et.atZone(ZoneId.systemDefault()))));
				odiv.appendChild(ediv);
			}
			
			return odiv;
		}
		else if (column.equals("status")) {
			var row = Html.span();
			
			switch(status) {
			case ERROR:
				row.appendChild(Html.i("fa-solid", "fa-circle-exclamation","text-danger"));
				break;
			case RUNNING:
				row.appendChild(Html.i("fa-solid", "fa-person-running","text-success"));
				break;
			case MISSING:
				row.appendChild(Html.i("fa-solid", "fa-circle-question","text-danger"));
				break;
			default:
				row.appendChild(Html.i("fa-solid", "fa-clock","text-primary"));
				break;
			}
			row.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "schedulerTask."+ status.name()).addClass("small"));
			return row;
		}
		return Html.span("");
	}

	@Override
	protected Class<SchedulerTask> getResourceClass() {
		return SchedulerTask.class;
	}

	@Override
	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	@Override
	public void handleError(ClusterID id, TaskSpec spec, DistributedTask<?> task, TaskCompletionContext context,
			Throwable exception) {
		sendEvent(id, task, () -> new SchedulerTaskCompleteEvent(getObjectByUUID(SchedulerTaskStorage.toUuid(id)), exception));
	}

	@Override
	public void handleSuccess(ClusterID id, TaskSpec spec, DistributedTask<?> task, TaskCompletionContext context) {
		sendEvent(id, task, () -> new SchedulerTaskCompleteEvent(getObjectByUUID(SchedulerTaskStorage.toUuid(id))));
	}

	private void sendEvent(ClusterID id, DistributedTask<?> task, Supplier<SystemEvent> evt) {
		task.classifiers().stream().findFirst().ifPresentOrElse(tenantUuid -> {
			tenantService.asSystem(() -> {
				var tenant = tenantService.getObjectByUUID(tenantUuid);
				tenantService.executeAs(tenant, () -> {
					try {
						eventService.publishEvent(evt.get());
					}
					catch(ObjectNotFoundException onfe) {
						LOG.warn("Failed to find task.", onfe);
					}
				});
			});
		}, () -> {
			LOG.warn("Task has no classifiers so tenant UUID cannot be determined.");
		});
	}
	
	private void configureTenantTaskBuilder(Builder bldr, TenantTask task) {
		var ttconfig = task.getClass().getAnnotation(TenantTaskConfig.class);
		bldr.addAttribute(SchedulerTask.ALLOW_CANCEL, ttconfig != null && ttconfig.allowCancel());
		bldr.addAttribute(SchedulerTask.ALLOW_RUN_NOW, ttconfig != null && ttconfig.allowRunNow());
		bldr.addAttribute(SchedulerTask.ALLOW_TENANT_RUN_NOW, ttconfig != null && ttconfig.allowTenantRunNow());
		bldr.addAttribute(SchedulerTask.ALLOW_TENANT_CANCEL, ttconfig != null && ttconfig.allowTenantCancel());
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		executor.close();
	}

	@Override
	public boolean isRunning() {
		return executor != null && executor.isShutdown();
	}

	@Override
	public boolean has(String path, Serializable key) {
		return executor.has(path, key);
	}

	@Override
	public Serializable get(String path, Serializable key) {
		return executor.get(path, key);
	}

	@Override
	public void put(String path, Serializable key, Serializable value) {
		executor.put(path, key, value);
	}

	@Override
	public boolean remove(String path, Serializable key) {
		return executor.remove(path, key);
	}

	@Override
	public <V extends Serializable> IdentifiableFuture<V> future(ClusterID clusterID) {
		return executor.future(clusterID);
	}

	@Override
	public boolean isLeader() {
		return executor.leader();
	}

	private SchedulerTaskStatus calcStatus(IdentifiableFuture<?> future, TaskInfo info) {
		if(info != null && info.lastError().isPresent()) {
			return SchedulerTaskStatus.ERROR;
		}
		else if(future == null) {
			return SchedulerTaskStatus.MISSING;
		}
		else {
			if(future.info().active()) {
				return SchedulerTaskStatus.RUNNING;
			}
			else {
				return SchedulerTaskStatus.WAITING;
			}
		}
	}

}
