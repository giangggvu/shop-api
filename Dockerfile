# Build stage: compiles the app with Maven, so nothing but Docker is needed on your machine.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Run stage: only the Java runtime and the built jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home shop
USER shop
COPY --from=build /app/target/shop-api-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
