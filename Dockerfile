FROM openjdk:13
COPY target/*.jar diplom-integration.jar
ENTRYPOINT java -jar diplom-integration.jar
EXPOSE 8080