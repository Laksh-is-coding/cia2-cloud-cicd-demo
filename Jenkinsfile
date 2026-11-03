pipeline {
    agent any

    environment {
        MAVEN_CMD = 'C:\\Users\\darks\\Downloads\\apache-maven-3.10.0-bin\\apache-maven-3.10.0\\bin\\mvn.cmd'
        DOCKER_EXE = 'C:\\Users\\darks\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
        GITLEAKS_EXE = 'C:\\Users\\darks\\AppData\\Local\\Microsoft\\WinGet\\Packages\\Gitleaks.Gitleaks_Microsoft.Winget.Source_8wekyb3d8bbwe\\gitleaks.exe'
        AZ_EXE = 'C:\\Program Files\\Microsoft SDKs\\Azure\\CLI2\\wbin\\az.cmd'

        DOCKER_IMAGE_NAME = 'laksh4950/cia2-cloud-cicd-demo'
        AZURE_RESOURCE_GROUP = 'cia2-cloud-cicd-demo-rg'
        AZURE_CONTAINER_APP = 'cia2-cloud-cicd-demo'
        AZURE_TENANT_ID = '5beb351c-3fb8-418f-b612-fe36ace96ef3'
        AZURE_SUBSCRIPTION_ID = '92649c85-7f8f-4e68-9bd0-fa87cb83d946'
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
                bat '"%MAVEN_CMD%" clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat '"%MAVEN_CMD%" org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=cia2-cloud-cicd-demo'
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                script {
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}"
                    env.DOCKER_IMAGE = "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}"
                }

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DH_USER',
                        passwordVariable: 'DH_TOKEN'
                    )
                ]) {
                    bat '"%DOCKER_EXE%" build -t "%DOCKER_IMAGE%" .'

                    bat '''
                        @echo off
                        setlocal
                        set "TOKEN_FILE=%WORKSPACE%\\docker-token.tmp"
                        <nul set /p "=%DH_TOKEN%" > "%TOKEN_FILE%"
                        "%DOCKER_EXE%" login --username "%DH_USER%" --password-stdin < "%TOKEN_FILE%"
                        set "LOGIN_EXIT=%ERRORLEVEL%"
                        del "%TOKEN_FILE%" >nul 2>&1
                        exit /b %LOGIN_EXIT%
                    '''

                    bat '"%DOCKER_EXE%" push "%DOCKER_IMAGE%"'
                }
            }
        }

        stage('Deploy to Azure Container Apps') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'azure-cia2-sp',
                        usernameVariable: 'AZURE_CLIENT_ID',
                        passwordVariable: 'AZURE_CLIENT_SECRET'
                    )
                ]) {
                    bat '''
                        @echo off

                        "%AZ_EXE%" login --service-principal ^
                            --username "%AZURE_CLIENT_ID%" ^
                            --password "%AZURE_CLIENT_SECRET%" ^
                            --tenant "%AZURE_TENANT_ID%"
                        if errorlevel 1 exit /b 1

                        "%AZ_EXE%" account set --subscription "%AZURE_SUBSCRIPTION_ID%"
                        if errorlevel 1 exit /b 1

                        echo Updating Azure Container App to image: %DOCKER_IMAGE%

                        "%AZ_EXE%" containerapp update ^
                            --name "%AZURE_CONTAINER_APP%" ^
                            --resource-group "%AZURE_RESOURCE_GROUP%" ^
                            --image "%DOCKER_IMAGE%"
                        if errorlevel 1 exit /b 1

                        echo Azure Container App update command completed successfully.

                        "%AZ_EXE%" containerapp show ^
                            --name "%AZURE_CONTAINER_APP%" ^
                            --resource-group "%AZURE_RESOURCE_GROUP%" ^
                            --query "properties.template.containers[0].image" ^
                            --output tsv
                        if errorlevel 1 exit /b 1

                        "%AZ_EXE%" logout
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully. Deployed image: ${env.DOCKER_IMAGE}"
        }

        failure {
            echo 'Pipeline failed. Check the stage console output above for the error.'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}