pipeline {
    agent any

    tools {
        maven 'maven'
    }

    stages {
        stage('Secret Scan - Gitleaks') {
            steps {
                bat 'docker run --rm -v "%CD%:/repo" zricethezav/gitleaks:latest detect --source=/repo --no-git --redact --exit-code 1'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
        }
    }
}