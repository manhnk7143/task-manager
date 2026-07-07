# syntax=docker/dockerfile:1.7-labs
FROM registry-01.cloud.cmctelecom.vn/dev-rnd-307-v5xwnfab2hgt/maven:3.8.3-openjdk-17 as build
WORKDIR /app
ARG MAVEN_OPTS="-Dmaven.repo.local=.cache/mvn"
RUN --mount=type=cache,id=redis-dbaas,target=.cache/ \
    --mount=type=bind,source=pom.xml,target=pom.xml \
    mvn dependency:resolve
RUN --mount=type=cache,id=redis-dbaas,target=.cache/ \
    --mount=type=bind,source=src/,target=src/ \
    --mount=type=bind,source=pom.xml,target=pom.xml \
    mvn clean install && \
    mv /app/target/ /opt/target/
FROM openjdk:17
COPY --from=build /opt/target/dbaas-0.0.1-SNAPSHOT.jar redis-as-service.jar
ENTRYPOINT exec java -jar redis-as-service.jar
#FROM registry-01.cloud.cmctelecom.vn/dev-rnd-307-v5xwnfab2hgt/maven:3.8.3-openjdk-17 as build
#WORKDIR /app
#COPY . .
#RUN mvn clean install
#FROM openjdk:17-alpine
#VOLUME /tmp

#COPY --from=build /app/target/dbaas-0.0.1-SNAPSHOT.jar redis-as-service.jar
#EXPOSE 8080
#EXPOSE 9090
#EXPOSE 9091
#ENTRYPOINT exec java -jar redis-as-service.jar
