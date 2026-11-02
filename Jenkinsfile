pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        DOCKERHUB_USER = 'laksh4950'
        DOCKER_IMAGE = 'laksh4950/cia2-cloud-cicd-demo:1.0'
        DOCKER_EXE = 'C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
        GITLEAKS_EXE = 'C:\\Users\\darks\\AppData\\Local\\Microsoft\\WinGet\\Packages\\Gitleaks.Gitleaks_Microsoft.Winget.Source_8wekyb3d8bbwe\\gitleaks.exe'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Gitleaks') {
            steps {
                bat '"%GITLEAKS_EXE%" detect --source . --redact --exit-code 1'
            }
        }

        stage('Maven Build and Test') {
            steps {
                bat 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=cia2-cloud-cicd-demo'
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    bat '"%DOCKER_EXE%" build -t "%DOCKER_IMAGE%" .'

                    powershell '''
                        $env:DOCKERHUB_TOKEN | & $env:DOCKER_EXE login `
                            --username $env:DOCKERHUB_USER `
                            --password-stdin
                    '''

                    bat '"%DOCKER_EXE%" push "%DOCKER_IMAGE%"'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check the stage console output above for the error.'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}