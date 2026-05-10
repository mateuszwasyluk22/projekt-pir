FROM eclipse-temurin:17-jre

LABEL maintainer="Projekt Semestralny - PRiR"
LABEL description="Calkowanie numeryczne - Spring Boot REST API"

WORKDIR /app

EXPOSE 8080

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
