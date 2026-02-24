# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy dependency manifests first for better layer caching.
# The actual source is copied separately so dependency layers are only
# invalidated when build files change, not on every source edit.
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Resolve dependencies in a separate layer
RUN gradle dependencies --no-daemon || true

# Copy source and build the fat jar
COPY src ./src
RUN gradle bootJar --no-daemon

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S druidic && adduser -S druidic -G druidic
USER druidic

# Copy only the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
