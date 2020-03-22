package com.jadaptive.app.scheculer;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class LockableTaskScheduler implements TaskScheduler {

	ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
	
	public LockableTaskScheduler() {
		scheduler.initialize();
	}
	
	@Override
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		return scheduler.schedule(task, trigger);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		return scheduler.schedule(task, startTime);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		return scheduler.scheduleAtFixedRate(task, startTime, period);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		return scheduler.scheduleAtFixedRate(task, period);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		return scheduler.scheduleWithFixedDelay(task, startTime, delay);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return scheduleWithFixedDelay(task, delay);
	}

	public void setPoolSize(Integer poolSize) {
		scheduler.setPoolSize(poolSize);
	}
}
