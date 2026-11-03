pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        DOCKER_IMAGE = 'laksh4950/cia2-cloud-cicd-demo'
        AZURE_RESOURCE_GROUP = 'cia2-cloud-cicd-demo-rg'
        AZURE_CONTAINER_APP = 'cia2-cloud-cicd-demo'

        // Tools installed on your Windows Jenkins machine
        MAVEN_HOME = 'C:\\Users\\darks\\Downloads\\apache-maven-3.10.0-bin\\apache-maven-3.10.0'
        GITLEAKS_EXE = 'C:\\Users\\darks\\AppData\\Local\\Microsoft\\WinGet\\Packages\\Gitleaks.Gitleaks_Microsoft.Winget.Source_8wekyb3d8bbwe\\gitleaks.exe'
        AZ_EXE = 'C:\\Program Files\\Microsoft SDKs\\Azure\\CLI2\\wbin\\az.cmd'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Gitleaks') {
            steps {
                bat '''
                    "%GITLEAKS_EXE%" detect --source . --redact --exit-code 1
                '''
            }
        }

        stage('Maven Build and Test') {
            steps {
                bat '''
                    "%MAVEN_HOME%\\bin\\mvn.cmd" clean verify
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat '''
                        "%MAVEN_HOME%\\bin\\mvn.cmd" org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=cia2-cloud-cicd-demo
                    '''
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                script {
                    def imageTag = "${env.BUILD_NUMBER}"

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'dockerhub-creds',
                            usernameVariable: 'DOCKERHUB_USER',
                            passwordVariable: 'DOCKERHUB_PASSWORD'
                        )
                    ]) {
                        bat """
                            @echo on
                            docker login --username "%DOCKERHUB_USER%" --password "%DOCKERHUB_PASSWORD%"
                            if errorlevel 1 exit /b 1

                            docker buildx build --platform linux/amd64 --load -t "${DOCKER_IMAGE}:${imageTag}" .
                            if errorlevel 1 exit /b 1

                            docker push "${DOCKER_IMAGE}:${imageTag}"
                            if errorlevel 1 exit /b 1

                            docker logout
                        """
                    }
                }
            }
        }

        stage('Deploy to Azure Container Apps') {
            steps {
                script {
                    def imageTag = "${env.BUILD_NUMBER}"

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'azure-cia2-sp',
                            usernameVariable: 'AZURE_CLIENT_ID',
                            passwordVariable: 'AZURE_CLIENT_SECRET'
                        )
                    ]) {
                        withEnv(["DEPLOY_IMAGE=${DOCKER_IMAGE}:${imageTag}"]) {
                            bat '''
                                @echo on

                                call "%AZ_EXE%" login --service-principal ^
                                    --username "%AZURE_CLIENT_ID%" ^
                                    --password "%AZURE_CLIENT_SECRET%" ^
                                    --tenant "%AZURE_TENANT_ID%"
                                if errorlevel 1 exit /b 1

                                call "%AZ_EXE%" account set --subscription "%AZURE_SUBSCRIPTION_ID%"
                                if errorlevel 1 exit /b 1

                                call "%AZ_EXE%" containerapp update ^
                                    --name "%AZURE_CONTAINER_APP%" ^
                                    --resource-group "%AZURE_RESOURCE_GROUP%" ^
                                    --image "%DEPLOY_IMAGE%"
                                if errorlevel 1 exit /b 1

                                call "%AZ_EXE%" logout
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed. Check the stage logs for the result.'
        }
    }
}