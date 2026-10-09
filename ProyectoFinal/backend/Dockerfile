# =============================================================================
# ETAPA 1: COMPILACIÓN CON MAVEN Y TEMURIN 21
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Descarga de dependencias en capa cacheada
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilación del paquete JAR de producción sin tests unitarios
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================================================
# ETAPA 2: RUNTIME LIGERO CON JRE 21 (Menos de 200 MB)
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Crear usuario sin privilegios por seguridad (OWASP)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copiar artefacto generado en la etapa de build
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Parámetros JVM optimizados para entornos con 512 MB de memoria (Free Tier de Render)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
