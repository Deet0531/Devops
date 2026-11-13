// Declarative Pipeline - Jenkins must have JDK 17 and Maven available on the agent
pipeline {
    agent any

    triggers {
        // CI trigger: poll Git every minute (use a GitHub webhook instead if Jenkins is publicly reachable)
        pollSCM('* * * * *')
    }

    environment {
        APP_NAME = 'todo-api'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Enhancement (versioning): short Git SHA + build number
                    env.GIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.VERSION   = "${env.BUILD_NUMBER}-${env.GIT_SHORT}"
                    echo "Building version ${env.VERSION}"
                }
            }
        }

        // Enhancement (automated tests): runs BEFORE the build
        stage('Test') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B package -DskipTests'
                // Versioned copy of the artifact, e.g. todo-api-12-a1b2c3d.jar
                sh 'cp target/${APP_NAME}.jar target/${APP_NAME}-${VERSION}.jar'
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Image (optional)') {
            when { expression { return fileExists('/var/run/docker.sock') } }
            steps {
                sh 'docker build -t ${APP_NAME}:${VERSION} .'
            }
        }
    }

    post {
        success { echo "Build ${env.VERSION} succeeded" }
        failure { echo 'Build failed - check console output' }
    }
}
