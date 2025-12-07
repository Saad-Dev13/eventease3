pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "eventease"
    }

    stages {
        stage('Pull Code') {
            steps {
                git url: 'https://github.com/Saad-Dev13/eventease3.git', branch: 'main'
            }
        }

        stage('Build & Run Docker Compose') {
            steps {
                script {
                    sh 'docker-compose down'
                    sh 'docker-compose up --build -d'
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished."
        }
    }
}
