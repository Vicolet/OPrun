# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17-slim AS build

# Installer OpenJDK 21 depuis Adoptium
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1%2B12/OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz && \
    mkdir -p /usr/lib/jvm && \
    tar -xzf OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz -C /usr/lib/jvm && \
    rm OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz

# Définir JAVA_HOME pour utiliser JDK 21
ENV JAVA_HOME=/usr/lib/jvm/jdk-21.0.1+12
ENV PATH="$JAVA_HOME/bin:${PATH}"

# Vérifier la version de Java
RUN java -version

# Set working directory in the container
WORKDIR /app

# Copy Maven configuration files and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn package clean -DskipTests

# Stage 2: Create a lightweight runtime image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

ENTRYPOINT bash

# Copy the compiled JAR from the build stage
COPY --from=build /app/oprun.jar ./oprun.jar

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "oprun.jar"]
