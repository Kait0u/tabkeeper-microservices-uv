FROM amazoncorretto:25-alpine
LABEL authors="kaito"

WORKDIR /app

COPY services/group-service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]