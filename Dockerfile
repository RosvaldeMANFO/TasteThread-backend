FROM gradle:7-jdk21 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./application.jar

EXPOSE 8080
CMD ["sh", "-c", "java -Dport=${PORT:-8080} -jar application.jar"]