FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

EXPOSE 8080

CMD ["java","-jar","target/hotel-management-1.0.0-SNAPSHOT.jar"]