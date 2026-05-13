# ==========================================
# ETAPA 1: CONSTRUCCIÓN (Build)
# ==========================================
# Usamos una imagen pesada que tiene Maven instalado para poder compilar el código
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos primero el pom.xml y descargamos dependencias (optimiza la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos el proyecto ignorando los tests (ya los pasamos en local)
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: EJECUCIÓN (Run)
# ==========================================
# Usamos una imagen de Java súper ligera (Alpine) solo para ejecutar la app, no para compilarla
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Extraemos el archivo .jar generado en la Etapa 1 y lo metemos en esta imagen ligera
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto por donde escucha Spring Boot
EXPOSE 8080

# El comando que ejecutará el servidor al encender el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]