pipeline {
    agent {
        docker { image 'node:16.13.1-alpine' }
        }

    stages {
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'node --version'
            }
        }
    }
}
// added