# Stage 1: Build the application
FROM eclipse-temurin:21.0.8_9-jdk-noble AS build

WORKDIR /build

# Cache Maven dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN ./mvnw -B package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21.0.8_9-jre-noble AS runtime
LABEL authors="krisnaajiep" version="3.0.0" description="Expense Tracker API Application"

WORKDIR /app

# Create a non-root user to run the application
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# Copy the built JAR file from the build stage
COPY --from=build /build/target/*.jar ./expense-tracker-api.jar

EXPOSE 8080

# Set the active Spring profile to 'prod' and define the entry point
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "expense-tracker-api.jar"]