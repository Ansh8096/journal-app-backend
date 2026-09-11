# ============================================================
# BUILD STAGE
# ============================================================

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven descriptor first.
# This improves Docker layer caching.
COPY pom.xml .

# Download dependencies before copying source.
RUN mvn -B dependency:go-offline

# Copy application source and resources.
COPY src ./src

# Build the Spring Boot executable JAR.
RUN mvn -B clean package -DskipTests


# ============================================================
# RUNTIME STAGE
# ============================================================

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the generated Spring Boot JAR.
COPY --from=build /app/target/*.jar app.jar

# Documentation/default port.
# Spring Boot will actually use ${PORT:8080}.
EXPOSE 8080

# Start application.
ENTRYPOINT ["java", "-jar", "app.jar"]