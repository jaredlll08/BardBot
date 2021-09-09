#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        jdk "jdk-16.0.1+9"
    }
    stages {
        stage('Deploy') {
            steps {
                sh "docker-compose up -d --build"
            }
        }
    }
    options {
        disableConcurrentBuilds()
    }
}
