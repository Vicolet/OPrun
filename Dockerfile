# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

# Set working directory in the container
WORKDIR /app

# Copy Maven configuration files and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/OPrun-1.0-SNAPSHOT.jar ./OPrun.jar

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "OPrun.jar"]
