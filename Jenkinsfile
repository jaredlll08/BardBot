#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        jdk "jdk8u292-b10"
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
