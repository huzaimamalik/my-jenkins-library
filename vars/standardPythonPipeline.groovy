def call(Map config) {
    pipeline {
        agent any
        
        parameters {
            booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Check to run SonarQube Analysis. Uncheck to skip.')
        }

        stages {
            stage('Clone Repository') {
                steps {
                    echo "Downloading code from ${config.repoUrl}..."
                    git branch: config.branch, url: config.repoUrl
                }
            }

            stage('Test Application') {
                steps {
                    echo 'Installing dependencies and running Unit Tests...'
                    sh '''
                        python3 -m venv venv
                        . venv/bin/activate
                        pip install -r requirements.txt
                        ./venv/bin/python -m pytest tests/ --cov=. --cov-report=xml
                    '''
                }
            }

            stage('SonarQube Analysis') {
                when {
                    expression { return params.RUN_SONAR == true }
                }
                steps {
                    script {
                        def scannerHome = tool 'sonar-scanner'
                        withSonarQubeEnv('SonarQube-Server') {
                            sh "${scannerHome}/bin/sonar-scanner"
                        }
                    }
                }
            }

            stage('Quality Gate Check') {
                when {
                    expression { return params.RUN_SONAR == true }
                }
                steps {
                    waitForQualityGate abortPipeline: true
                }
            }

            stage('Deploy Application') {
                steps {
                    echo 'Deploying as Linux Daemon...'
                    sh '''
                        # 1. Define the path
                        APP_DIR="/opt/fastapi-backend"
                        
                        # 2. Sync files to the target
                        sudo /usr/bin/mkdir -p $APP_DIR
                        sudo /usr/bin/rsync -av --exclude='.git' ./ $APP_DIR/
                        sudo /usr/bin/chown -R huz:www-data $APP_DIR
                        
                        # 3. Update dependencies
                        cd $APP_DIR
                        if [ ! -d "venv" ]; then
                            python3 -m venv venv
                        fi
                        . venv/bin/activate
                        pip install -r requirements.txt
                        
                        # 4. Restart the daemon
                        sudo /usr/bin/systemctl daemon-reload
                        sudo /usr/bin/systemctl restart fastapi
                    '''
                }
            }
        }

        post {
            success {
                echo 'Deployment Successful!'
            }
            failure {
                echo 'Pipeline failed! Sending email alert...'
                mail to: config.alertEmail,
                     subject: "Jenkins Build Failed: ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}]",
                     body: "Your FastAPI deployment failed. \n\nCheck the console logs here to see the error: \n${env.BUILD_URL}"
            }
        }
    }
}
