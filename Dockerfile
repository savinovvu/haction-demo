# Использовать образ с Java 21
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Даем права на выполнение gradlew
RUN chmod +x gradlew

RUN ./gradlew build -x test

# Runtime stage тоже с Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /app/build/libs/*.jar app.jar
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]