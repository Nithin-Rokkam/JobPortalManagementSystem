FROM maven:3.9.6-eclipse-temurin-21 AS builder
ENV TZ=UTC
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Djacoco.skip=true -q \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.http.connectionTimeOut=120000 \
    -Dmaven.wagon.http.readTimeOut=120000


FROM eclipse-temurin:21-jre
ENV TZ=UTC
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
