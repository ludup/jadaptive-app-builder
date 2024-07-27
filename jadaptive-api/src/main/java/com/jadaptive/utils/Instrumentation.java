package com.jadaptive.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Instrumentation {
	
	public interface TimedHandle extends Closeable {
		@Override
		void close();
	}

	private static Logger LOG = LoggerFactory.getLogger(Instrumentation.class);

	public final static boolean ENABLE_TIMINGS = Boolean.getBoolean("jadaptive.timings");
	public final static long THRESHOLD = Long.parseLong(System.getProperty("jadaptive.timings.threshold", "100"));
	
	private final static ThreadLocal<AtomicInteger> depth = new ThreadLocal<>();
	
	public static TimedHandle timed(String name) {
		if(ENABLE_TIMINGS) {
			var started = Instant.now();
			var aint = increaseDepth();
			return new TimedHandle() {
				@Override
				public void close() {
					var ended = Instant.now();
					var took = ended.toEpochMilli() - started.toEpochMilli();
					if(took >= THRESHOLD) {
						LOG.warn("{}{} took {} ms", indent(aint), name, took);
					}
					decreaseDepth();
				}
			};
		}
		else {
			return new TimedHandle() {
				@Override
				public void close() {
				}
			};
		}
	}
	
	private static String indent(AtomicInteger depth) {
		var c = ( depth.get() - 1 ) * 2;
		var b = new StringBuilder();
		for(int i = 0 ; i < c; i++) {
			b.append(' ');
		}
		return b.toString();
	}
	
	private static AtomicInteger increaseDepth() {
		var aint = depth.get();
		if(aint == null) {
			aint = new AtomicInteger();
			depth.set(aint);
		}
		aint.incrementAndGet();
		return aint;
	}
	
	private static AtomicInteger decreaseDepth() {
		var aint = depth.get();
		if(aint.decrementAndGet() == 0) {
			depth.remove();
		}
		return aint;
	}

	public static <T> T callUnchecked(String name, Callable<T> task) {
		try {
			return call(name, task);
		} catch (RuntimeException re) {
			throw re;
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T call(String name, Callable<T> task) throws Exception {
		if(ENABLE_TIMINGS) {
			var started = Instant.now();
			var aint = increaseDepth();
			try {
				return task.call();
			} finally {
				var ended = Instant.now();
				var took = ended.toEpochMilli() - started.toEpochMilli();
				if(took >= THRESHOLD) {
					LOG.warn("{}{} took {} ms", indent(aint), name, took);
				}
				decreaseDepth();
	
			}
		}
		else {
			return task.call();
		}
	}

	public static void run(String name, Runnable task) {
		if(ENABLE_TIMINGS) {
			var started = Instant.now();
			var aint = increaseDepth();
			try {
				task.run();
			} finally {
				var ended = Instant.now();
				var took = ended.toEpochMilli() - started.toEpochMilli();
				if(took >= THRESHOLD) {
					LOG.warn("{}{} took {} ms", indent(aint), name, took);
				}
				decreaseDepth();
			}
		}
		else {
			task.run();
		}
	}
}
