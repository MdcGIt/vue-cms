package com.ruoyi.common.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ruoyi.async")
public class AsyncProperties {

	private final Pool pool = new Pool();

	private final Shutdown shutdown = new Shutdown();

	public Pool getPool() {
		return this.pool;
	}
	
	public Shutdown getShutdown() {
		return this.shutdown;
	}

	public static class Pool {

		private String threadNamePrefix = "RyTask-";

		/**
		 * Queue capacity. An unbounded capacity does not increase the pool and
		 * therefore ignores the "max-size" property.
		 */
		private int queueCapacity = 100;

		/**
		 * Core number of threads.
		 */
		private int coreSize = 8;

		/**
		 * Maximum allowed number of threads. If tasks are filling up the queue, the
		 * pool can expand up to that size to accommodate the load. Ignored if the queue
		 * is unbounded.
		 */
		private int maxSize = 20;

		/**
		 * Whether core threads are allowed to time out. This enables dynamic growing
		 * and shrinking of the pool.
		 */
		private boolean allowCoreThreadTimeout = true;

		/**
		 * Time limit for which threads may remain idle before being terminated.
		 */
		private Duration keepAlive = Duration.ofSeconds(60);

		public String getThreadNamePrefix() {
			return this.threadNamePrefix;
		}

		public void setThreadNamePrefix(String threadNamePrefix) {
			this.threadNamePrefix = threadNamePrefix;
		}

		public int getQueueCapacity() {
			return this.queueCapacity;
		}

		public void setQueueCapacity(int queueCapacity) {
			this.queueCapacity = queueCapacity;
		}

		public int getCoreSize() {
			return this.coreSize;
		}

		public void setCoreSize(int coreSize) {
			this.coreSize = coreSize;
		}

		public int getMaxSize() {
			return this.maxSize;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}

		public boolean isAllowCoreThreadTimeout() {
			return this.allowCoreThreadTimeout;
		}

		public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout) {
			this.allowCoreThreadTimeout = allowCoreThreadTimeout;
		}

		public Duration getKeepAlive() {
			return this.keepAlive;
		}

		public void setKeepAlive(Duration keepAlive) {
			this.keepAlive = keepAlive;
		}
	}

	public static class Shutdown {

		/**
		 * Whether the executor should wait for scheduled tasks to complete on shutdown.
		 */
		private boolean awaitTermination = true;

		/**
		 * Maximum time the executor should wait for remaining tasks to complete. Default: 120s.
		 */
		private Duration awaitTerminationPeriod = Duration.ofSeconds(120);

		public boolean isAwaitTermination() {
			return this.awaitTermination;
		}

		public void setAwaitTermination(boolean awaitTermination) {
			this.awaitTermination = awaitTermination;
		}

		public Duration getAwaitTerminationPeriod() {
			return this.awaitTerminationPeriod;
		}

		public void setAwaitTerminationPeriod(Duration awaitTerminationPeriod) {
			this.awaitTerminationPeriod = awaitTerminationPeriod;
		}

	}

}
