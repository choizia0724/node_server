FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project

# wrapper + 스크립트 먼저
COPY --chown=gradle:gradle gradlew gradlew
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts ./
RUN chmod +x ./gradlew || true

# 의존성 캐시
RUN ./gradlew --no-daemon build -x test || true

# 소스 복사 후 실제 빌드
COPY --chown=gradle:gradle . .
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
