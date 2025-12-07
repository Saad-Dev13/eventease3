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
                sh 'ls -la testcases/ || echo "testcases directory not found"'
            }
        }

        stage('Build & Run Docker Compose') {
            steps {
                sh '''
                  docker-compose down || true
                  docker-compose up --build -d
                '''
            }
        }

        stage('Test (Selenium)') {
            steps {
                sh '''
                  echo "=== Verifying testcases directory on host ==="
                  ls -la testcases/

                  echo "=== Running Maven tests in container ==="
                  docker run --rm \
                    -v "$PWD":/workspace \
                    -w /workspace/testcases \
                    markhobson/maven-chrome:jdk-11 \
                    bash -c "cp -r /workspace/testcases/* . 2>/dev/null; ls -la; mvn test -DbaseUrl=http://16.171.139.26:5173"
                '''
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
    }
}
