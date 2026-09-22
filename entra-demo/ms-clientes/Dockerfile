# ===== Etapa 1: Build =====
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copia primero el pom para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y compila
COPY src ./src
RUN mvn clean package -DskipTests

# ===== Etapa 2: Runtime =====
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]