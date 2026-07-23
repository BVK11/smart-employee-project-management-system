FROM ubuntu:latest
LABEL authors="KARTHIKEYA"

ENTRYPOINT ["top", "-b"]

# ---------- Build Stage ----------
FROM maven:3.9.11-amazoncorretto-25 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ---------- Runtime Stage ----------
FROM amazoncorretto:25

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]