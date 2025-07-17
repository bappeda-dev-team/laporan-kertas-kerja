FROM gradle:8.14.2-jdk21-ubi AS builder
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle bootJar

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
