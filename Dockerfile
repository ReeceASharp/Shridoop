# Dockerfile to build a Java 8 image with Maven 3.9.0 & java 8 by default, newer versions can be used by changing the ARGs, but aren't verified to work

# Use the official maven/Java 8 image to create a build artifact.

ARG JAVA_VERSION=8
FROM maven:3.9.0-eclipse-temurin-$JAVA_VERSION
WORKDIR /app

ADD pom.xml pom.xml
# TODO: Move this around so that build dependencies are cached in a better way
ADD src/ src/

RUN ["mvn", "package"]

ENTRYPOINT ["java", "-cp", "./target/Shridoop-1.0-jar-with-dependencies.jar"]