FROM gradle:7.2.0-jdk16 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle fatJar --no-daemon

FROM openjdk:16-slim

RUN mkdir /app
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar BardBot-1.0.0.jar

ENTRYPOINT ["java", "-jar","BardBot-1.0.0.jar"]