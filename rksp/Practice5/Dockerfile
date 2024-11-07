FROM openjdk:22
WORKDIR /app
COPY target/Practice5-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 80
CMD ["java", "-Dspring.datasource.url=jdbc:postgresql://host.docker.internal:5432/file", "-jar", "app.jar"]
