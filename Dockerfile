# syntax=docker/dockerfile:1.7-labs
FROM maven:3.6.3-jdk-11 as build
WORKDIR /app
ARG MAVEN_OPTS="-Dmaven.repo.local=.cache/mvn"
RUN --mount=type=cache,id=cloudops-taskmanager,target=.cache/ \
    --mount=type=bind,source=pom.xml,target=pom.xml \
    mvn dependency:resolve
RUN --mount=type=cache,id=cloudops-taskmanager,target=.cache/ \
    --mount=type=bind,source=src/,target=src/ \
    --mount=type=bind,source=pom.xml,target=pom.xml \
    mvn clean install && \
    mv /app/target/ /opt/target/
FROM openjdk:11
COPY --from=build /opt/target/task_manager-1.0-SNAPSHOT.jar task_manager.jar
COPY --link ./config /config
ENTRYPOINT exec java -jar task_manager.jar
EXPOSE 9091


#FROM maven:3.6.3-jdk-11 as build
#WORKDIR /app
#COPY . .
#RUN mvn clean install

#FROM openjdk:11
#COPY --from=build /app/target/task_manager-1.0-SNAPSHOT.jar task_manager.jar
#COPY ./config /config
#ENTRYPOINT exec java -jar task_manager.jar
#EXPOSE 9091
