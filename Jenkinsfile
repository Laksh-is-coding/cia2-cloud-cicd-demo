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
                    bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.3.0.6276:sonar -Dsonar.projectKey=cia2-cloud-cicd-demo'
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    bat '"C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" build -t %DOCKERHUB_USER%/cia2-cloud-cicd-demo:1.0 .'
                    bat 'echo %DOCKERHUB_TOKEN% | "C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" login -u %DOCKERHUB_USER% --password-stdin'
                    bat '"C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" push %DOCKERHUB_USER%/cia2-cloud-cicd-demo:1.0'
                }
            }
        }
    }
}