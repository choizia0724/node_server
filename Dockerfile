# ---- Build ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle --no-daemon clean bootJar -x test

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
