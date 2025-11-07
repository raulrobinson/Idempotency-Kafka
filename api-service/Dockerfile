FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY build.gradle settings.gradle* gradlew ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies || true
COPY . .
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]