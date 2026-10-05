# Build the executable Spring Boot JAR with Java 21.
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN sh ./mvnw -B -DskipTests package

# To run tests during the build, replace the RUN command above with this one.
# The application context test needs environment variables and a reachable test database.
# RUN sh ./mvnw -B package

# Keep only the Java runtime and application JAR in the final image.
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/flightdetector-0.0.1-SNAPSHOT.jar app.jar

USER 10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
