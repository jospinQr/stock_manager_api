FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp

RUN apk add --no-cache netcat-openbsd

COPY build/libs/StockManagerApi-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for-mysql.sh wait-for-mysql.sh
RUN chmod +x wait-for-mysql.sh

EXPOSE 8080
ENTRYPOINT ["./wait-for-mysql.sh"]
