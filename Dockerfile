# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM gradle:8-jdk17 AS build

WORKDIR /app

# Copy gradle files first for better layer caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (this layer will be cached if build.gradle doesn't change)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application (skip tests for faster build)
RUN gradle clean build -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# JVM options for better performance in containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
