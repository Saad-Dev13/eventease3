pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "eventease"
        STUDENT_EMAIL = "saad0934003@gmail.com"  
        TEACHER_EMAIL = "qasimalik@gmail.com"  
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
                    docker.image('markhobson/maven-chrome:latest').inside('--network host -e SE_OFFLINE=true') {
                        dir('testcases') {
                            sh 'pwd && ls -la'
                            sh 'mvn test -DbaseUrl=http://16.171.139.26:5173'
                        }
                    }
                }
            }
        }
        
        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            script {
                def raw = sh(
                    script: "grep -h \"<testcase\" testcases/target/surefire-reports/*.xml || echo ''",
                    returnStdout: true
                ).trim()

                int total = 0
                int passed = 0
                int failed = 0
                int skipped = 0

                def details = ""

                if (raw) {
                    raw.split('\n').each { line ->
                        if (line.trim()) {
                            total++

                            def nameMatcher = (line =~ /name=\"([^\"]+)\"/)
                            def name = nameMatcher ? nameMatcher[0][1] : "Unknown Test"

                            if (line.contains("<failure")) {
                                failed++
                                details += "${name} — FAILED\n"
                            } else if (line.contains("<skipped") || line.contains("</skipped>")) {
                                skipped++
                                details += "${name} — SKIPPED\n"
                            } else {
                                passed++
                                details += "${name} — PASSED\n"
                            }
                        }
                    }
                }

                def emailBody = """
EventEase Test Summary (Build #${env.BUILD_NUMBER})

Repository: eventease3
Branch: main
Commit: ${env.GIT_COMMIT ?: 'N/A'}

Total Tests:   ${total}
Passed:        ${passed}
Failed:        ${failed}
Skipped:       ${skipped}

Detailed Results:
${details}

Build URL: ${env.BUILD_URL}
"""

                emailext(
                    to: "${env.STUDENT_EMAIL}, ${env.TEACHER_EMAIL}",
                    subject: "[EventEase] Build #${env.BUILD_NUMBER} - ${currentBuild.result ?: 'SUCCESS'}",
                    body: emailBody
                )
                
                echo "Pipeline finished. Email sent to ${env.STUDENT_EMAIL} and ${env.TEACHER_EMAIL}"
            }
        }
    }
}
