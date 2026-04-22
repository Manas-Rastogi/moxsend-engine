
#Stage 1Build the JAR

FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .

#  Give executable permission to mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the JAR skip tests
# RUN mvnw clean package
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true


#Stage 2Run the JAR
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=builder /app/target/*.jar app.jar

#Expose application port
EXPOSE 8080

#Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]