pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "eventease"
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                checkout scm
                sh 'echo "=== Repository root contents ===" && pwd && ls -la'
                sh 'echo "=== testcases directory ===" && ls -la testcases/ || echo "testcases directory not found"'
            }
        }

        stage('Build & Run Docker Compose') {
            steps {
                sh 'echo "=== Stopping any existing containers ==="'
                sh 'docker-compose down || true'

                sh 'echo "=== Building and starting containers ==="'
                sh 'docker-compose up --build -d'
            }
        }

        stage('Test (Selenium)') {
            steps {
                script {
                    docker.image('selenium/standalone-chrome:latest').inside('--network host -v /dev/shm:/dev/shm') {
                        sh '''
                          apt-get update && apt-get install -y maven
                          cd testcases
                          mvn test -DbaseUrl=http://16.171.139.26:5173
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
    }
}
