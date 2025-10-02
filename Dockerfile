# ---- Build ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project

# 전체 복사 (필요시 .dockerignore로 node_modules 등 제외)
COPY --chown=gradle:gradle . .

# 로그 자세히 + wrapper/gradle 자동 선택
RUN set -eux; \
  ls -la || true; \
  ls -la gradle/wrapper || true; \
  TOOL=gradle; [ -f ./gradlew ] && chmod +x ./gradlew && TOOL=./gradlew; \
  $TOOL --no-daemon -v; \
  $TOOL --no-daemon tasks --all | grep -E 'bootJar|assemble|build' || true; \
  # 먼저 컴파일 에러를 명확히 드러냄
  $TOOL --no-daemon --stacktrace --info --warning-mode all compileKotlin || { echo '--- compileKotlin failed ---'; exit 1; }; \
  # 이후 실제 빌드
  $TOOL --no-daemon --stacktrace --info --warning-mode all clean bootJar -x test

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
