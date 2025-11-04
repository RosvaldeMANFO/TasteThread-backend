FROM gradle:7-jdk21 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew buildFatJar --no-daemon

FROM gradle:7-jdk21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./application.jar
EXPOSE 8080
CMD ["java", "-jar", "application.jar"]