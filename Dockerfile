# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /app

# Copy the wrapper and pom.xml first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from the build stage (adjust the jar name if needed, but *.jar usually works for spring boot)
COPY --from=build /app/target/*.jar app.jar

# Expose the port your spring boot app runs on
EXPOSE 8083

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
