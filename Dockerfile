# ---- Build ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
# gradle wrapper가 있다면 ./gradlew 사용 가능. 여기선 gradle 이미지라 gradle 명령도 OK
RUN ./gradlew --no-daemon clean bootJar -x test || gradle --no-daemon clean bootJar -x test

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
