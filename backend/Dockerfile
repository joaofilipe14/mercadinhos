# ==========================================
# Execução Pura e Leve
# ==========================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Recebe o nome do microsserviço enviado pelo Docker Compose
ARG SERVICE_NAME
# Copia o JAR diretamente da pasta target local do teu computador
COPY ${SERVICE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]