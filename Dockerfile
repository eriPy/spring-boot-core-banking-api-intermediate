FROM eclipse-temurin:25-jdk-alpine
WORKDIR /bank
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline
COPY . .
EXPOSE 8080
CMD ["./mvnw", "spring-boot:run"]