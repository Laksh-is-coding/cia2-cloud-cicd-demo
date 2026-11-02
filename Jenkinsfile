pipeline {
    agent any

    tools {
        maven 'maven'
    }

    stages {
        stage('Secret Scan - Gitleaks') {
            steps {
                bat '"C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" run --rm -v "%CD%:/repo" zricethezav/gitleaks:latest detect --source=/repo --no-git --redact --exit-code 1'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn -B sonar:sonar -Dsonar.projectKey=cia2-cloud-cicd-demo'
                }
            }
        }
    }
}