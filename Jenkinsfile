pipeline {
    agent any 
    environment {
        APP_NAME = 'node-server-app'
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out code from GitHub...'
                git branch: 'main', url: 'https://github.com/choizia0724/node_server.git'
                echo 'Code successfully checked out.'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image from Dockerfile...'
                script {
                    def imageTagLatest = "${env.APP_NAME}:latest"
                    def imageTagBuild = "${env.APP_NAME}:${env.BUILD_NUMBER}"

                    sh "docker build -t ${imageTagLatest} -t ${imageTagBuild} ."
                    echo "Docker images built: ${imageTagLatest}, ${imageTagBuild}"

                    // k3s containerd에 이미지 바로 로드 (sudo 필요)
                    sh """
                    docker save ${imageTagLatest} -o ${env.APP_NAME}.tar
                    sudo ctr --address /run/k3s/containerd/containerd.sock images import ${env.APP_NAME}.tar
                    rm -f ${env.APP_NAME}.tar
                    """
                }
            }
        }

        stage('Prepare kubectl') {
            steps {
                echo 'Downloading kubectl...'
                script {
                    sh 'curl -LO https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl'
                    sh 'chmod +x kubectl'

                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to Kubernetes...'
                sh 'kubectl apply -f kubernetes/deployment.yaml'
                sh "kubectl get pods -l app=${env.APP_NAME}"
                sh 'kubectl get services'
                echo 'Deployment to Kubernetes complete.'
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
        success {
            echo 'Build and deployment successful!'
        }
        failure {
            echo 'Build or deployment failed! Check console output for details.'
        }
    }
}
