# syntax=docker/dockerfile:1

FROM maven:3-adoptopenjdk-11

WORKDIR /app

COPY java-producer ./java-producer

WORKDIR /app/java-producer

CMD ["mvn"]
