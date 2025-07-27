# Dockerfile
FROM node:20-alpine 
# Node.js 20 버전의 Alpine Linux 이미지 사용

WORKDIR /app 
# 작업 디렉토리 설정

COPY package.json ./ 
# package.json 파일 복사
RUN npm install --production  
# 의존성 설치 (생산 환경용)

COPY script.js ./  
# 스크립트 파일 복사

CMD ["node", "script.js"]   
# 컨테이너 시작 시 실행할 명령어 설정