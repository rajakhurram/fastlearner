pipeline {
    agent any

    environment {
        PATH = "/usr/local/bin:/usr/bin:/bin:$HOME/.npm-global/bin"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/qaximbalti/fastlearner-frontend.git'
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm ci'  // faster and reliable than npm install
            }
        }

        stage('Build Angular App') {
            steps {
                sh 'ng build --configuration=production'
            }
        }

        stage('Deploy to Nginx') {
            steps {
                // Assuming Jenkins user has write access to /var/www/angular-app
                sh '''
                    rm -rf /var/www/angular-app/*
                    cp -r dist/fast-learner-app/* /var/www/angular-app/
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Angular app deployed successfully!'
        }
        failure {
            echo '❌ Deployment failed. Check console output.'
        }
    }
}
