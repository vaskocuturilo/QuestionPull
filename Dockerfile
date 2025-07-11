FROM maven:3.9.9-amazoncorretto-17-alpine AS temp_build_image

ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME

COPY pom.xml .

COPY src ./src

RUN mvn clean package

FROM openjdk:17-jdk-slim

ENV ARTIFACT_NAME=QuestionPull-0.0.1-SNAPSHOT.jar

ENV APP_HOME=/usr/app

WORKDIR $APP_HOME

COPY --from=temp_build_image $APP_HOME/target/$ARTIFACT_NAME app.jar

RUN addgroup --system app && adduser --system --ingroup app app \
    && mkdir -p /usr/app/logs \
    && chown -R app:app /usr/app

USER app

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
