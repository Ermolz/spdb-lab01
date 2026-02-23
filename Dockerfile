FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle gradle
COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
