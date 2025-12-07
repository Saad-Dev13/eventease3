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
                    docker.image('maven:3.8.1-openjdk-17').inside('--network host -v /dev/shm:/dev/shm') {
                        sh '''
                          apt-get update && apt-get install -y wget gnupg
                          wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
                          sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'
                          apt-get update && apt-get install -y google-chrome-stable
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
