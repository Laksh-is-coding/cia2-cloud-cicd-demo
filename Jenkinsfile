pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        DOCKERHUB_USER = 'laksh4950'
        DOCKER_IMAGE = 'laksh4950/cia2-cloud-cicd-demo:1.0'
        DOCKER_EXE = 'C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
    }

    stages {
        stage('Gitleaks') {
            steps {
                bat 'gitleaks detect --source . --no-git'
            }
        }

        stage('Maven Clean Verify') {
            steps {
                bat 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar -Dsonar.projectKey=cia2-cloud-cicd-demo'
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                script {
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'dockerhub-creds',
                            usernameVariable: 'DOCKERHUB_USER',
                            passwordVariable: 'DOCKERHUB_TOKEN'
                        )
                    ]) {
                        bat "\"${DOCKER_EXE}\" build -t ${DOCKER_IMAGE} ."

                        powershell '''
                            $ErrorActionPreference = "Stop"
                            $env:DOCKERHUB_TOKEN |
                                & $env:DOCKER_EXE login --username $env:DOCKERHUB_USER --password-stdin
                            if ($LASTEXITCODE -ne 0) {
                                exit $LASTEXITCODE
                            }
                        '''

                        bat "\"${DOCKER_EXE}\" push ${DOCKER_IMAGE}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
    }
}