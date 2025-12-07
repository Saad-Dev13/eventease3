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
                  
                  echo "=== Running Maven tests using tar to copy files ==="
                  tar -czf /tmp/testcases.tar.gz testcases/
                  
                  cat > /tmp/run_tests.sh << 'EOF'
cd /tmp
tar -xzf testcases.tar.gz
cd /tmp/testcases
mvn test -DbaseUrl=http://16.171.139.26:5173
EOF
                  chmod +x /tmp/run_tests.sh
                  docker run --rm --network host -v /tmp:/tmp -w /workspace maven:3.8.1-openjdk-17 /tmp/run_tests.sh
                '''
            }
        }    }

    post {
        always {
            echo 'Pipeline finished.'
        }
    }
}
