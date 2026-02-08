# Multi-stage build for Reddit Backend
# Stage 1: Build the application
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Install dependencies for building
RUN apk add --no-cache maven

# Copy pom.xml and download dependencies (for layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre-alpine

# Create non-root user for security
RUN addgroup -S lambrk && adduser -S lambrk -G lambrk

# Install necessary packages
RUN apk add --no-cache curl

WORKDIR /app

# Copy dependencies from builder
COPY --from=builder /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=builder /app/target/dependency/META-INF /app/META-INF
COPY --from=builder /app/target/dependency/BOOT-INF/classes /app

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R lambrk:lambrk /app

# Switch to non-root user
USER lambrk

# Expose port
EXPOSE 9500

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9500/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+OptimizeStringConcat \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom \
               --enable-preview"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp '/app:/app/lib/*' com.example.lambrk.RedditBackendApplication"]
