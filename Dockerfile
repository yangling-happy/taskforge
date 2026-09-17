# 多阶段构建：先 Gradle 编译，再拷贝 jar 到 JRE 镜像
FROM gradle:8.6-jdk17 AS build
WORKDIR /workspace
COPY settings.gradle.kts build.gradle.kts ./
COPY common common
COPY scheduler scheduler
COPY worker worker
RUN gradle --no-daemon :scheduler:bootJar :worker:bootJar

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/scheduler/build/libs/*.jar /app/scheduler.jar
COPY --from=build /workspace/worker/build/libs/*.jar /app/worker.jar
# 默认起 scheduler，worker 容器通过 command 覆盖
ENTRYPOINT ["java", "-jar", "/app/scheduler.jar"]
