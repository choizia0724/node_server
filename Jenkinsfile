// Jenkinsfile
pipeline {
  agent any
  environment {
      REGISTRY      = 'docker.io/choizia'
      IMAGE_NAME    = 'node-server-app'
      IMAGE         = "${REGISTRY}/${IMAGE_NAME}"
  }

  stages {
    stage('Checkout Code') {
      steps {
        git branch: 'backend', url: 'https://github.com/choizia0724/node_server.git'
      }
    }

    stage('Docker Build & Push (Docker Hub)') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'DOCKERHUB',
                                          usernameVariable: 'DOCKERHUB_USER',
                                          passwordVariable: 'DOCKERHUB_PASS')]) {
          sh '''
            set -eux
            echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin

            # 플랫폼 자동감지 (arm64 / amd64)
            ARCH=$(uname -m)
            if [ "$ARCH" = "aarch64" ] || [ "$ARCH" = "arm64" ]; then
              PLATFORM=linux/arm64
            else
              PLATFORM=linux/amd64
            fi

            TAG_BUILD="${IMAGE}:${BUILD_NUMBER}"
            TAG_LATEST="${IMAGE}:latest"

            # buildx 준비
            docker buildx create --use --name multi-builder || true
            docker buildx inspect --bootstrap

            # 멀티태그로 빌드 & 푸시 (tar 불필요)
            docker buildx build \
              --platform "$PLATFORM" \
              -t "$TAG_BUILD" -t "$TAG_LATEST" \
              . --push
          '''
        }
      }
    }


    stage('Deploy to Kubernetes') {
      steps {
        sh '''
          set -eux
          # 반드시 repo의 최신 파일을 적용
          kubectl apply -f kubernetes/deployment.yaml

          # 이미지 태그는 빌드번호로 업데이트
          kubectl set image deployment/node-server-deployment \
            node-server-container=docker.io/choizia/node-server-app:latest

          kubectl rollout status deployment/node-server-deployment --timeout=180s
        '''
      }
    }
  }

  post {
    success { echo 'Build & push & deploy: SUCCESS' }
    failure { echo 'FAILED — console output를 확인하세요' }
  }
}
