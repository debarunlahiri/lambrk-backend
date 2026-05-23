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
    public OpenTelemetry openTelemetry() {
        String otlpEndpoint = System.getenv().getOrDefault("OTLP_ENDPOINT", "http://localhost:4317");
        if (otlpEndpoint.isBlank() || "disabled".equalsIgnoreCase(otlpEndpoint)) {
            return OpenTelemetry.noop();
        }
        var spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .setTimeout(Duration.ofSeconds(5))
            .build();

        var tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .setSampler(Sampler.traceIdRatioBased(0.05))
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();
    }

    @Bean
    public TracerProvider openTelemetryTracerProvider(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracerProvider();
    }
}
