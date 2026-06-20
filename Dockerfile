# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the frontend so it's available
COPY frontend ./frontend

# Copy backend source code
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src/

# Build the backend
WORKDIR /app/backend
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy frontend
COPY --from=build /app/frontend ./frontend

# Copy backend JAR
COPY --from=build /app/backend/target/event-management-system-1.0-SNAPSHOT.jar ./backend/app.jar

# Expose the port
EXPOSE 7070

# Set working directory to backend so that ../frontend works
WORKDIR /app/backend
ENTRYPOINT ["java", "-cp", "app.jar", "com.ems.Main"]
