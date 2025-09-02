// Jenkinsfile
pipeline {
  agent any
  environment {
    APP_NAME   = 'node-server-app'
    DH_USER    = 'choizia'
    IMAGE      = "docker.io/${DH_USER}/${APP_NAME}"
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
          # 빌드번호 태그로 롤아웃 → 새 이미지로 반드시 업데이트됨
          kubectl set image deployment/node-server-deployment \
            node-server-container=${IMAGE}:${BUILD_NUMBER}

          # 롤아웃 완료까지 대기
          kubectl rollout status deployment/node-server-deployment --timeout=120s

          # 확인용
          kubectl get pods -l app=${APP_NAME} -o wide
        '''
      }
    }
  }

  post {
    success { echo 'Build & push & deploy: SUCCESS' }
    failure { echo 'FAILED — console output를 확인하세요' }
  }
}
