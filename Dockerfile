# Use the official Gradle image as the base image
FROM gradle:8.3-jdk17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy all project files into the container
COPY . /app

# Ensure the Gradle wrapper is executable
RUN chmod +x gradlew

# Run Gradle build with additional flags
RUN ./gradlew clean build --no-daemon --stacktrace --refresh-dependencies
