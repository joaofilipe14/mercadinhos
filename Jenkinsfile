pipeline {
    agent any

    environment {
        // Define o caminho para o projeto backend
        BACKEND_DIR = 'backend/mercados-service'
    }

    stages {
        stage('🚀 Inicialização') {
            steps {
                echo 'A iniciar o pipeline do Ecossistema Mercadinhos...'
            }
        }

        stage('🧪 Testes & Cobertura') {
            steps {
                // Executa o Maven dentro de um contentor isolado com JDK 21
                // Isto evita que tenhas de instalar o Maven dentro do Jenkins
                sh """
                    docker run --rm -v \$(pwd):/app -w /app/${BACKEND_DIR} maven:3.9-eclipse-temurin-21 \
                    mvn clean test
                """
            }
        }
    }

    post {
        always {
            // Captura os relatórios de testes JUnit para mostrar no painel
            junit allowEmptyResults: true, testResults: "**/target/surefire-reports/*.xml"

            // O plugin do Jenkins lê o relatório do JaCoCo e monta os gráficos
            jacoco execPattern: '**/target/*.exec',
                   classPattern: '**/target/classes',
                   sourcePattern: '**/src/main/java',
                   inclusionPattern: '**/*.class'
        }
        success {
            echo '🎉 Todos os testes passaram com sucesso e a cobertura foi registada!'
        }
        failure {
            echo '❌ O build ou os testes falharam. Verifica os logs acima.'
        }
    }
}