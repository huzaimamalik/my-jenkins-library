def call(Map config) {
    pipeline {
        agent any

        parameters {
            booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Check to run SonarQube Analysis. Uncheck to skip.')
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm 
                }
            }

            stage('Test & Coverage') {
                steps {
                    echo "Running tests for ${config.projectName}..."
                }
            }

            stage('SonarQube Analysis') {
                when {
                    expression { return params.RUN_SONAR == true }
                }
                steps {
                    echo "Executing SonarQube for ${config.projectName}..."
                }
            }

            stage('Build Docker Image') {
                steps {
                    echo "Building Docker image for ${config.projectName}..."
                }
            }
        }
    }
}
