# Dockerfile
FROM node:22


# Oracle Instant Client 설치
RUN apt-get update && apt-get install -y unzip libaio1 wget \
    && wget https://download.oracle.com/otn_software/linux/instantclient/instantclient-basiclite-linux.x64-21.9.0.0.0.zip \
    && unzip instantclient-basiclite-linux.x64-21.9.0.0.0.zip -d /opt/oracle/ \
    && rm instantclient-basiclite-linux.x64-21.9.0.0.0.zip \
    && ln -s /opt/oracle/instantclient_21_9 /opt/oracle/instantclient

ENV LD_LIBRARY_PATH=/opt/oracle/instantclient
ENV PATH=/opt/oracle/instantclient

# 컨테이너 내에서 애플리케이션의 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# package.json과 package-lock.json 파일을 먼저 복사합니다.
COPY package*.json ./

# Node.js 의존성(모듈)을 설치합니다.
RUN npm install --production

COPY . .

# 애플리케이션이 리스닝하는 포트를 외부에 노출합니다.
EXPOSE 3000

CMD ["npm", "run", "start"]
