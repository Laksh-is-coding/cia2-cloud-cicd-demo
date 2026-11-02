pipeline {
    agent any

    tools {
        maven 'maven'
    }

    stages {
        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
        }
    }
}