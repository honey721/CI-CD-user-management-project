# 1. Base Image: Lightweight Java 17 JRE Alpine
FROM eclipse-temurin:17-jre-alpine

# 2. Set Working Directory inside Container
WORKDIR /app

# 3. Copy pre-compiled JAR file from target folder
COPY target/notification-audit-service-1.0.0.jar app.jar

# 4. Expose Application Port
EXPOSE 8082

# 5. Startup Execution Command
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
