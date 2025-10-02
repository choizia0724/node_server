# ---- Build ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle --no-daemon :web:clean :web:bootJar -x test

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport" \
    SPRING_PROFILES_ACTIVE=prod
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
