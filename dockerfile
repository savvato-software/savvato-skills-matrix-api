FROM openjdk:19-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the application jar file to the container
COPY target/skills-matrix-api-*.jar /app.jar

# Expose the port on which the application is running
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
