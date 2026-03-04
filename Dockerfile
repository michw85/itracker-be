# syntax = docker/dockerfile:1.4

############################
# 1) Build stage
############################
FROM eclipse-temurin:17-jdk AS builder
LABEL stage=builder

WORKDIR /workspace/app

# 1.1) Copy only wrapper + build scripts to leverage cache
COPY gradlew settings.gradle build.gradle ./
COPY gradle/wrapper gradle/wrapper

RUN chmod +x gradlew

# 1.2) Pre-fetch dependencies (cached)
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/workspace/app/.gradle \
    ./gradlew --no-daemon dependencies

# 1.3) Copy source and build the fat JAR
COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/workspace/app/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

# 1.4) Rename the generated JAR to a fixed name
RUN cp build/libs/*.jar app.jar

############################
# 2) Runtime stage
############################
FROM gcr.io/distroless/java17-debian11:nonroot AS runtime
LABEL stage=runtime

WORKDIR /app

# 2.1) Copy единственный jar из builder
COPY --from=builder /workspace/app/app.jar ./app.jar

# 2.2) Открываем порт, если нужно
EXPOSE 8080

# 2.3) Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
