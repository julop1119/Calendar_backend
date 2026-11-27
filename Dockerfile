# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# wrapper/build 설정만 먼저 복사해서 Gradle 배포판 + 의존성 다운로드를 캐시 레이어로 분리한다.
# (소스만 바뀌는 평소 빌드에서는 이 레이어가 캐시돼서 매번 다시 받지 않는다)
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies -x test || true

COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar -x test
RUN cp $(ls build/libs/*.jar | grep -v plain | head -n1) /app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
