# Dockerfile
# Node.js 22 버전의 Alpine 리눅스 기반 이미지를 사용합니다. (경량화된 이미지)
FROM node:22-alpine

# 컨테이너 내에서 애플리케이션의 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# package.json과 package-lock.json 파일을 먼저 복사합니다.
COPY package*.json ./

# Node.js 의존성(모듈)을 설치합니다.
RUN npm install --production

COPY . .

# 애플리케이션이 리스닝하는 포트를 외부에 노출합니다.
EXPOSE 3000

CMD ["node", "src/index.js"]