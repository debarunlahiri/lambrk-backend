package com.lambrk.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry ->
        registry.config().commonTags("application", "lambrk-backend", "version", "1.0.0");
  }

  @Bean
  public JvmMemoryMetrics jvmMemoryMetrics() {
    return new JvmMemoryMetrics();
  }

  @Bean
  public JvmThreadMetrics jvmThreadMetrics() {
    return new JvmThreadMetrics();
  }

  @Bean
  public ProcessorMetrics processorMetrics() {
    return new ProcessorMetrics();
  }

  @Bean
  public UptimeMetrics uptimeMetrics() {
    return new UptimeMetrics();
  }

  @Bean
  public HealthIndicator virtualThreadHealthIndicator(MeterRegistry meterRegistry) {
    return () -> {
      try {
        // Check if virtual threads are available and working
        var count = meterRegistry.get("jvm.threads.live").gauge().value();
        var status = count > 0 ? Status.UP : Status.DOWN;

        return org.springframework.boot.actuate.health.Health.status(status)
            .withDetail("liveThreads", count)
            .withDetail("virtualThreadsEnabled", true)
            .build();
      } catch (Exception e) {
        return org.springframework.boot.actuate.health.Health.down(e)
            .withDetail("virtualThreadsEnabled", false)
            .build();
      }
    };
  }
}
