// Jenkinsfile
pipeline {
    agent any 
    // 환경 변수 정의
    environment {
        APP_NAME = 'node-server-app'
    }

    stages {
        stage('Checkout Code') { // 1단계: GitHub 저장소에서 코드 가져오기
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

                // containerd에 이미지 로드 (k3s가 인식하게)
                sh """
                docker save ${imageTagLatest} -o ${env.APP_NAME}.tar
                sudo ctr images import ${env.APP_NAME}.tar
                rm -f ${env.APP_NAME}.tar
                """
            }
        }
    }


        stage('Prepare kubectl') { // 새로운 스테이지: kubectl 준비
            steps {
                echo 'Downloading kubectl...'
                script {
                    // kubectl 다운로드 및 실행 권한 부여
                    sh 'curl -LO https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl'
                    sh 'chmod +x kubectl'
                }
            }
        }

        stage('Deploy to Kubernetes') { // 3단계: Kubernetes에 배포
            steps {
                echo 'Deploying to Kubernetes...'
                
                sh 'kubectl apply -f kubernetes/deployment.yaml'
                sh "kubectl get pods -l app=${env.APP_NAME}" 
                sh 'kubectl get services'   
                echo 'Deployment to Kubernetes complete.'
            }
        }
    }

    post { // 파이프라인 완료 후 처리
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
