package com.jadaptive.app.scheduler;

import static com.jadaptive.app.scheduler.Jobs.formatDisplayDuration;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.scheduler.ScheduledTask;
import com.jadaptive.api.scheduler.ScheduledTaskConfig;
import com.jadaptive.api.scheduler.SchedulerService;
import com.jadaptive.api.scheduler.SchedulerTask;
import com.jadaptive.api.scheduler.SchedulerTask.SchedulerTaskStatus;
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

@Service
public class SchedulerServiceImpl extends AbstractUUIDObjectServceImpl<SchedulerTask>  implements SchedulerService, TenantAware, StartupAware {

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);
	
	@Autowired
	private DistributedScheduledExecutor executor;	
	
	@Autowired
	private App applicationService; 
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private TaskScheduler taskScheduler;
	
	@Autowired
	private I18nService i18nService;
	
	@Override
	public void initializeSystem(boolean newSchema) {
		initializeTenant(getCurrentTenant(), newSchema);
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
		if(log.isInfoEnabled()) {
			log.info("Scheduling tasks for {}", tenant.getName());
		}
		
		for(var task  : applicationService.getBeans(ScheduledTask.class)) {
			if(task.isSystemOnly() && !tenant.isSystem()) {
				continue;
			}
			
			var cron = ScheduledTask.AT_MIDNIGHT;
			var annotation = task.getClass().getAnnotation(ScheduledTaskConfig.class);
			if(annotation != null) {
				cron = annotation.value();
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
				onConflict(ConflictResolution.IGNORE).
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
		executor.futureOr(ClusterID.parse(uuid)).ifPresentOrElse(ftr -> ftr.cancel(mayInterrupt), () -> log.warn("Request to cancel task {} that does not exist.", uuid));
	}

	@Override
	public void runScheduledTaskNow(String uuid) {
		// TODO Auto-generated method stub
		log.warn("RUN SCHEDULED TASK NOW NOT IMPLEMENTED!");
	}

	@Override
	public void onApplicationStartup() {
		
		eventService.deleted(Tenant.class, (evt)-> {
			for(var future : new ArrayList<>(executor.futures())) {
				if(future.classifiers().contains(evt.getObject().getUuid())) {
					/* NOTE: my hunch is it better not to interrupt if this does happen, let it fail if the realm if gone and the task needs it.
					 The task is best place to deal with this as it sees fit.
					*/  
					future.cancel(false);
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
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {

		var tsk = getObjectByUUID(obj.getUuid());
		var cid = ClusterID.parse(tsk.getId());
		var future = executor.future(cid);
		var info = future == null ? null : future.info();
		
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

			Element div3 = null;
			if(info != null && info.progress().isPresent()) {
				div3 = Html.div("progress", "auto-progress-bar").
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
			}

			
			var odiv = Html.div();
			odiv.appendChild(div1);
			odiv.appendChild(div2);
			if(div3 != null) {
				odiv.appendChild(div3);
			}
			return odiv;
		}
		else if (column.equals("details")) {
			switch(tsk.getSchedule()) {
			case TRIGGER:
				return Html.i18n(SchedulerTask.RESOURCE_KEY, "details.trigger", info.spec().trigger().toString());
			case NOW:
				return Html.i18n(SchedulerTask.RESOURCE_KEY, "details.now");
			case ONE_SHOT:
				return Html.i18n(SchedulerTask.RESOURCE_KEY, "details.oneShot", formatDisplayDuration(Duration.ofMillis(info.spec().initialDelay())));
			case FIXED_DELAY:
				return Html.i18n(SchedulerTask.RESOURCE_KEY, "details.fixedDelay", formatDisplayDuration(Duration.ofMillis(info.spec().initialDelay())), formatDisplayDuration(Duration.ofMillis(info.spec().period())));
			case FIXED_RATE:
				return Html.i18n(SchedulerTask.RESOURCE_KEY, "details.fixedDelay", formatDisplayDuration(Duration.ofMillis(info.spec().initialDelay())), formatDisplayDuration(Duration.ofMillis(info.spec().period())));
			}
		}
		else if (column.equals("status")) {
			var row = Html.span();
			
			SchedulerTaskStatus status;
			if(future == null) {
				status = SchedulerTaskStatus.MISSING;
			}
			else {
				if(future.info().active()) {
					status = SchedulerTaskStatus.RUNNING;
				}
				else {
					status = SchedulerTaskStatus.WAITING;
				}
			}
			
			switch(status) {
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
			row.appendChild(Html.i18n(SchedulerTask.RESOURCE_KEY, "schedulerTask."+ status.name()).addClass("ms-3"));
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
	
	private void configureTenantTaskBuilder(Builder bldr, TenantTask task) {
		var ttconfig = task.getClass().getAnnotation(TenantTaskConfig.class);
		bldr.addAttribute(SchedulerTask.ALLOW_CANCEL, ttconfig != null && ttconfig.allowCancel());
		bldr.addAttribute(SchedulerTask.ALLOW_RUN_NOW, ttconfig != null && ttconfig.allowRunNow());
		bldr.addAttribute(SchedulerTask.ALLOW_TENANT_RUN_NOW, ttconfig != null && ttconfig.allowTenantRunNow());
		bldr.addAttribute(SchedulerTask.ALLOW_TENANT_CANCEL, ttconfig != null && ttconfig.allowTenantCancel());
	}

}
