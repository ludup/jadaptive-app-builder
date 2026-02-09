package com.jadaptive.api.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.permissions.FeatureGroup;
import com.jadaptive.api.permissions.LicensedFeature;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.DynamicColumn;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;
import com.sshtools.gardensched.Affinity;
import com.sshtools.gardensched.ConflictResolution;
import com.sshtools.gardensched.Schedule;

@ObjectDefinition(resourceKey = SchedulerTask.RESOURCE_KEY, scope = ObjectScope.GLOBAL, creatable = false, updatable = false, deletable = false)
@ObjectServiceBean(bean = SchedulerService.class)
@TableView(defaultColumns = { "status", "displayName", "affinity", "schedule", "details" }, otherColumns = {
		@DynamicColumn(resourceKey = "displayName", service = SchedulerService.class),
		@DynamicColumn(resourceKey = "status", service = SchedulerService.class),
		@DynamicColumn(resourceKey = "details", service = SchedulerService.class), }, multipleDelete = false)
@Permissions(keys = { "ViewScheduler" })
@TableAction(bundle = SchedulerTask.RESOURCE_KEY, defaultAction = true, icon = "fa-person-running-fast", resourceKey = "runTaskNow", target = Target.ROW, url = "/app/api/scheduled-tasks/run-now/{uuid}", filter = SchedulerTaskRunNowActionFilter.class)
@TableAction(bundle = SchedulerTask.RESOURCE_KEY, defaultAction = false, icon = "fa-trash", resourceKey = "cancelTask", target = Target.ROW, url = "/app/api/scheduled-tasks/cancel/{uuid}", filter = SchedulerTaskCancelActionFilter.class)
@PageMenu(parent = ApplicationMenuService.REPORTING_MENU_UUID, icon = "fa-list-check", weight = 2000, withPermission = "ViewScheduler")
@LicensedFeature(group = FeatureGroup.FREE, value = Session.RESOURCE_KEY)
public final class SchedulerTask extends UUIDEntity {

	public static final String ALLOW_TENANT_CANCEL = "allowTenantCancel";
	public static final String ALLOW_TENANT_RUN_NOW = "allowTenantRunNow";
	public static final String ALLOW_RUN_NOW = "allowRunNow";
	public static final String ALLOW_CANCEL = "allowCancel";

	private static final long serialVersionUID = 4112203668306516160L;

	public static final String RESOURCE_KEY = "schedulerTask";

	public enum SchedulerTaskStatus {
		WAITING, RUNNING, MISSING, ERROR
	}

	public enum SchedulerTaskType {
		RUNNABLE, CALLABLE
	}

	@ObjectField(type = FieldType.TEXT)
	private String id;

	@ObjectField(type = FieldType.TEXT)
	private String submitter;

	@ObjectField(type = FieldType.ENUM)
	private Affinity affinity;

	@ObjectField(type = FieldType.TEXT)
	private String task;

	@ObjectField(type = FieldType.TEXT)
	private String trigger;

	@ObjectField(type = FieldType.ENUM)
	private Schedule schedule;

	@ObjectField(type = FieldType.LONG)
	private long initialDelay;

	@ObjectField(type = FieldType.LONG)
	private long period;

	@ObjectField(type = FieldType.ENUM)
	private TimeUnit timeUnit;

	@ObjectField(type = FieldType.ENUM)
	private ConflictResolution conflictResolution;

	@ObjectField(type = FieldType.TEXT)
	private List<String> classifiers;

	@ObjectField(type = FieldType.TIMESTAMP)
	private Date submitted;

	@ObjectField(type = FieldType.ENUM)
	private SchedulerTaskType taskType;

	@ObjectField(type = FieldType.TEXT)
	private String key;

	@ObjectField(type = FieldType.TEXT)
	private String bundle;

	@ObjectField(type = FieldType.TEXT)
	private String name;

	@ObjectField(type = FieldType.TEXT)
	private String displayName;

	@ObjectField(type = FieldType.TEXT)
	private Collection<String> attributes = new ArrayList<>();


	public Collection<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Collection<String> attributes) {
		this.attributes = attributes;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getId() {
		return id;
	}

	public void setHostname(String id) {
		this.id = id;
	}

	public Affinity getAffinity() {
		return affinity;
	}

	public void setAffinity(Affinity affinity) {
		this.affinity = affinity;
	}

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getClassifiers() {
		return classifiers;
	}

	public void setClassifiers(List<String> classifiers) {
		this.classifiers = classifiers;
	}

	public ConflictResolution getConflictResolution() {
		return conflictResolution;
	}

	public void setConflictResolution(ConflictResolution conflictResolution) {
		this.conflictResolution = conflictResolution;
	}

	public Date getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Date submitted) {
		this.submitted = submitted;
	}

	public SchedulerTaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(SchedulerTaskType taskType) {
		this.taskType = taskType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

}
