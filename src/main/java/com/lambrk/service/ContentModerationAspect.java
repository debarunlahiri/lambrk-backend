package com.lambrk.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ContentModerationAspect {

    private final AIContentModerationService moderationService;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    public ContentModerationAspect(AIContentModerationService moderationService,
                                 MeterRegistry meterRegistry,
                                 Tracer tracer) {
        this.moderationService = moderationService;
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
    }

    @Around("@annotation(ModerateContent)")
    public Object moderateContent(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract content from method arguments
        Object[] args = joinPoint.getArgs();
        String content = extractContent(args);
        String contentType = extractContentType(args);

        if (content != null && contentType != null) {
            // Perform AI moderation
            AIContentModerationService.ModerationResult result = 
                moderationService.moderateContent(content, contentType);

            // Record metrics
            meterRegistry.counter("content.moderation.attempts", 
                "type", contentType).increment();
            
            if (!result.approved()) {
                meterRegistry.counter("content.moderation.rejected", 
                    "type", contentType, 
                    "reason", String.join(",", result.categories())).increment();
                
                // Add trace context
                if (tracer.currentSpan() != null) {
                    tracer.currentSpan().tag("content.moderated", "true");
                    tracer.currentSpan().tag("moderation.reason", result.reason());
                }
                
                throw new ContentModerationException(
                    "Content rejected: " + result.reason(), 
                    result.categories()
                );
            }
            
            meterRegistry.counter("content.moderation.approved", 
                "type", contentType).increment();
        }

        // Proceed with original method
        return joinPoint.proceed();
    }

    private String extractContent(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        return null;
    }

    private String extractContentType(Object[] args) {
        // Determine content type based on method signature or context
        return "text"; // Default, could be enhanced
    }
}
