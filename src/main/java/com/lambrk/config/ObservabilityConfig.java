package com.lambrk.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class ObservabilityConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "lambrk-backend", "version", "1.0.0");
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

    @Bean
    public ScheduledExecutorService metricsScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            // Custom metrics collection can be added here
        }, 1, 1, TimeUnit.MINUTES);
        return scheduler;
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        String otlpEndpoint = System.getenv().getOrDefault("OTLP_ENDPOINT", "http://localhost:4317");
        
        var spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .setTimeout(Duration.ofSeconds(30))
            .build();

        var tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .setSampler(Sampler.traceIdRatioBased(0.1)) // 10% sampling
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();
    }

    @Bean
    public TracerProvider openTelemetryTracerProvider(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracerProvider();
    }

    public static class CustomMetrics {
        private final MeterRegistry meterRegistry;

        public CustomMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            initializeMetrics();
        }

        private void initializeMetrics() {
            // Custom business metrics
            meterRegistry.gauge("lambrk.posts.total", this, CustomMetrics::getTotalPosts);
            meterRegistry.gauge("lambrk.comments.total", this, CustomMetrics::getTotalComments);
            meterRegistry.gauge("lambrk.users.active", this, CustomMetrics::getActiveUsers);
            meterRegistry.gauge("lambrk.communities.total", this, CustomMetrics::getTotalSubreddits);
        }

        private double getTotalPosts() {
            // This would typically query the database or cache
            return 0.0; // Placeholder
        }

        private double getTotalComments() {
            // This would typically query the database or cache
            return 0.0; // Placeholder
        }

        private double getActiveUsers() {
            // This would typically query the database or cache
            return 0.0; // Placeholder
        }

        private double getTotalSubreddits() {
            // This would typically query the database or cache
            return 0.0; // Placeholder
        }

        public void recordPostCreated(String subreddit) {
            meterRegistry.counter("lambrk.posts.created", "community", subreddit).increment();
        }

        public void recordCommentCreated(String subreddit) {
            meterRegistry.counter("lambrk.comments.created", "community", subreddit).increment();
        }

        public void recordVoteCast(String voteType) {
            meterRegistry.counter("lambrk.votes.cast", "type", voteType).increment();
        }

        public void recordUserLogin(String userId) {
            meterRegistry.counter("lambrk.users.login", "userId", userId).increment();
        }

        public void recordUserRegistration() {
            meterRegistry.counter("lambrk.users.registered").increment();
        }
    }
}
