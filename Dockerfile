# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts ./

RUN chmod +x gradlew

# 의존성만 먼저 내려받아 캐시화 (소스는 아직 복사 X)
RUN ./gradlew --no-daemon dependencies > /dev/null || true

# 이제 소스 복사
COPY src src

# 테스트 스킵하고 fat jar 생성 (필요시 테스트 실행으로 변경)
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# (옵션) 보안상 비루트 사용자
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 결과 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# (옵션) 프로파일/메모리 등 런타임 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
