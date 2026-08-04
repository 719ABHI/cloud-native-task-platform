#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
#COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java","-jar","app.jar"]

#------------------------------------------------------------------------------------------------------------------------

# ==========================
# Stage 1 - Build the project
# ==========================
FROM gradle:9.5.1-jdk21 AS builder

WORKDIR /app

# # Copy Gradle build files
# COPY build.gradle .
# COPY settings.gradle .
# COPY gradle gradle


# # Copy source code
# COPY src src

# # Build Spring Boot JAR
# RUN gradle bootJar --no-daemon


# Copy Gradle wrapper and configuration files first 
COPY gradlew . 
COPY gradle gradle 
COPY build.gradle . 
COPY settings.gradle . 

# Give execute permission to Gradle Wrapper 
RUN chmod +x gradlew 

# Download dependencies (cached if build files don't change) -> Think of --no-daemon as telling Gradle to "Do your job, then turn off immediately." 
RUN ./gradlew dependencies --no-daemon 

# Copy application source code 
COPY src src 
# Build Spring Boot executable JAR 
RUN ./gradlew bootJar --no-daemon

# ==========================
# Stage 2 - Runtime Image
# ==========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]