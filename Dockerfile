### BUILD
#FROM openjdk:8-jdk-alpine as build
FROM gradle:jdk-alpine as build
COPY --chown=gradle:gradle . .

RUN gradle test installDist --stacktrace

### RUN
FROM openjdk:8-jre-alpine

RUN mkdir -p /opt/analyzer

COPY --from=build /home/gradle/build/install /opt/analyzer
COPY bin/analyze.sh /opt/analyzer/bin/analyze.sh

WORKDIR /opt/analyzer

ENTRYPOINT ["/opt/analyzer/bin/analyze.sh"]