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
                sh '''
                  echo "=== Verifying testcases directory inside Jenkins container ==="
                  pwd
                  ls -la testcases/

                  echo "=== Preparing /tmp/testcases host directory ==="
                  rm -rf /tmp/testcases
                  mkdir -p /tmp/testcases
                  cp -r testcases/* /tmp/testcases/

                  echo "=== Contents of /tmp/testcases on host ==="
                  ls -la /tmp/testcases

                  echo "=== Running Maven tests in markhobson/maven-chrome ==="
                  docker run --rm \
                    -v /tmp/testcases:/usr/src/app \
                    -w /usr/src/app \
                    markhobson/maven-chrome:jdk-11 \
                    mvn test -DbaseUrl=http://16.171.139.26:5173
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
