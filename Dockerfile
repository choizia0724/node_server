# ---- Build ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project

# wrapper & 빌드 스크립트 먼저 복사 (캐시 안정)
COPY --chown=gradle:gradle gradlew gradlew
COPY --chown=gradle:gradle gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY --chown=gradle:gradle gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x ./gradlew || true

# 의존성 캐시 워밍업 (소스 없이도 가능)
RUN ./gradlew --no-daemon build -x test || true

# 애플리케이션 소스
COPY --chown=gradle:gradle src src

# 실제 빌드
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
