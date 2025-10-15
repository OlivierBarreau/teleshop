# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn -q -DskipTests dependency:go-offline 
COPY src ./src
# Package application (.jar) creation : skip tests for faster build
RUN mvn -q -DskipTests package
# pick the boot jar (exclude original-*.jar)
RUN bash -lc 'JAR=$(ls target/*.jar | grep -v original) && cp "$JAR" app.jar'

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
