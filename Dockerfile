FROM openjdk:8-jdk-alpine
MAINTAINER Nikos Triantafyllou
VOLUME /tmp
ADD ./target/ESMO-SESSION-MNGR-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=container","-jar","/app.jar"]
EXPOSE 8080
