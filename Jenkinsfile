pipeline {
  agent any
  stages {
    stage('Pull Code') {
      steps {
        git credentialsId: '679a7714-823c-4226-a6f3-d0da635aa422', url: 'https://github.com/saaddev13/your-eventease2-repo.git'
      }
    }
    stage('Build and Run (Docker Compose)') {
      steps {
        sh 'docker-compose -f docker-compose.yml up --build -d'
      }
    }
  }
  triggers {
    githubPush()
  }
}

