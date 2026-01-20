FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# copy maven files first for better caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
