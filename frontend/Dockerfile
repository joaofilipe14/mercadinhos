# ==========================================
# Etapa 1: Compilação do Angular (Imagem estável Debian)
# ==========================================
FROM node:20 AS build
WORKDIR /app

# Copiar os ficheiros de dependências
COPY package*.json ./

# Executar o install com tratamento de erros de rede e cache limpa
RUN npm install --loglevel verbose

# Copiar o resto do código do frontend
COPY . .

# Gerar o build de produção do Angular
RUN npm run build -- --configuration=production

# ==========================================
# Etapa 2: Servidor Web Nginx
# ==========================================
FROM nginx:1.25-alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]