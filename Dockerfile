# Multi-stage build for Java application
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy gradle wrapper and dependencies
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Copy buildSrc
COPY buildSrc buildSrc

# Copy all subprojects
COPY common common
COPY core core
COPY adapter adapter
COPY app app
COPY report report

# Build the application (specify which app to build via ARG)
ARG APP_MODULE=api
RUN ./gradlew :${APP_MODULE}:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR
ARG APP_MODULE=api
COPY --from=build /app/app/${APP_MODULE}/build/libs/*.jar app.jar

# Create non-root user
RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

