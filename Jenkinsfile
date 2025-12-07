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
                sh 'echo "=== Repository root contents ===" && ls -la'
                sh 'echo "=== testcases directory ===" && ls -la testcases/ || echo "testcases directory not found"'
            }
        }

        stage('Build & Run Docker Compose') {
            steps {
                sh '''
                  echo "=== Stopping any existing containers ==="
                  docker-compose down || true

                  echo "=== Building and starting containers ==="
                  docker-compose up --build -d
                '''
            }
        }

        stage('Test (Selenium)') {
            steps {
                sh '''
                  echo "=== Verifying testcases directory on host ==="
                  ls -la testcases/
                  
                  echo "=== Installing Maven (if not present) ==="
                  which mvn || (apt-get update && apt-get install -y maven)
                  
                  echo "=== Running Maven tests directly on host ==="
                  cd testcases
                  mvn test -DbaseUrl=http://16.171.139.26:5173
                '''
            }
        }    }

    post {
        always {
            echo 'Pipeline finished.'
        }
    }
}
