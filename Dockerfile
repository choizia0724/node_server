# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle gradlew gradlew
COPY --chown=gradle:gradle gradle  gradle
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x ./gradlew || true

RUN ./gradlew --no-daemon --stacktrace --info --warning-mode all \
    build -x test || true

COPY --chown=gradle:gradle src src

RUN ./gradlew --no-daemon --stacktrace --info --warning-mode all \
    clean bootJar -x test

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
