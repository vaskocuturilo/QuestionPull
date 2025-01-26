FROM maven:3.9.9-amazoncorretto-17-alpine AS TEMP_BUILD_IMAGE

ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests || return 0

FROM eclipse-temurin:17-jdk-alpine

ENV ARTIFACT_NAME=QuestionPull-0.0.1-SNAPSHOT.jar

ENV APP_HOME=/usr/app

WORKDIR $APP_HOME

COPY --from=TEMP_BUILD_IMAGE $APP_HOME/target/$ARTIFACT_NAME app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]