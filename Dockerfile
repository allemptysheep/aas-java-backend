FROM eclipse-temurin:21-jre

WORKDIR /app

# JAR 파일 복사 (버전 명시)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 6443

# Spring Boot는 환경변수를 자동으로 읽음 (SERVER_PORT 등)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
