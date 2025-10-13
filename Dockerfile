# syntax=docker/dockerfile:1.6

FROM maven:3.9.6-eclipse-temurin-21 AS base
WORKDIR /workspace
COPY pom.xml .
COPY mvnw mvnw
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B -DskipTests dependency:go-offline

FROM base AS test
COPY src src
RUN ./mvnw -q -B test

FROM base AS builder
COPY src src
RUN ./mvnw -q -B -DskipTests package

FROM base AS development
COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=UTC \
    OLLAMA_API_URL=http://ollama:11434
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


