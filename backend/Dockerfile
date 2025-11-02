## Backend Dockerfile (multi-stage build)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom and resolve dependencies first for better caching
COPY backend/pom.xml ./
COPY backend/.mvn ./.mvn
COPY backend/mvnw ./mvnw
RUN ./mvnw -q -Dmaven.test.skip=true dependency:go-offline || true

# Copy sources and build
COPY backend/src ./src
RUN ./mvnw -q -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre
ENV JAVA_OPTS="" \
    SERVER_PORT=8080
WORKDIR /app
COPY --from=builder /app/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1
CMD ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
