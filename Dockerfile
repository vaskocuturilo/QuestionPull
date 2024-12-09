FROM openjdk:17-jdk-slim

ENV ARTIFACT_NAME=QuestionPull-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/target

# Copy application JAR
COPY $APP_HOME/$ARTIFACT_NAME app.jar

# Copy resources (e.g., questions.json) into the JAR’s classpath
COPY src/main/resources /target/resources

ENTRYPOINT ["java", "-jar", "app.jar"]
