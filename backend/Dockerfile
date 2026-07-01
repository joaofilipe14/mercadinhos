# ==========================================
# ETAPA 1: Compilação isolada com JDK 21
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS builder
ARG SERVICE_NAME
WORKDIR /build

# Copia o código fonte do backend para dentro do contentor
COPY . .

# Executa a compilação real gerando o JAR em Java 21
RUN mvn -pl ${SERVICE_NAME} clean package -DskipTests

# ==========================================
# ETAPA 2: Imagem de execução leve com JRE 21
# ==========================================
FROM eclipse-temurin:21-jre-alpine
ARG SERVICE_NAME
WORKDIR /app

# Vai buscar o JAR fresquinho gerado na Etapa 1
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]