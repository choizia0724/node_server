<img src="https://capsule-render.vercel.app/api?type=waving&color=BDBDC8&height=150&section=header" />

# 📈 Stock Chart Dashboard

## 1. PageLink

[🔗 To Deploy Web Page](https://portfolio-page-lyart-two.vercel.app/)

## 2. Stacks

- #### Frontend
  ![js](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=JavaScript&logoColor=white)
  ![nextJs](https://img.shields.io/badge/Next.js-000?logo=nextdotjs&logoColor=fff&style=for-the-badge)
  ![react](https://img.shields.io/badge/react-61DAFB?logo=react&logoColor=fff&style=for-the-badge)
  ![tailwindcss](https://img.shields.io/badge/TailwindCSS-06B6D4?logo=TailwindCSS&logoColor=fff&style=for-the-badge)
  ![axios](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)
  ![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)
  ![WebSocket](https://img.shields.io/badge/WebSocket-C93CD7?style=for-the-badge&logo=WebSocket&logoColor=white)
- #### Backend
  ![js](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=JavaScript&logoColor=white)
  ![axios](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)
  ![nodeJs](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)
  ![WebSocket](https://img.shields.io/badge/WebSocket-C93CD7?style=for-the-badge&logo=WebSocket&logoColor=white)
- #### Database & Infra & DevOps
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=PostgreSQL&logoColor=white)
  ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white)
  ![kubernetes](https://img.shields.io/badge/kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
  ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=Jenkins&logoColor=white)
  ![OracleCloud](https://img.shields.io/badge/OracleCloud-F38020?style=for-the-badge&logo=OracleCloud&logoColor=white)
  


## 3. Project Overview
- 프로젝트 이름: Stock Chart Dashboard
- 기간: 2025년 8월~ 2025년 9월
- 개발 인원: 개인 프로젝트
- 목적 및 배경:
   - 한국투자증권(KIS) API를 활용하여 실시간 주식/ETF 데이터를 시각화
   -  개인 투자 전략(단타·스윙·ETF 투자)을 데이터 기반으로 최적화
   -  코스피/코스닥 종목, ETF, 시가총액 Top20 추적 및 실시간 체결강도 확인  
- 주요 기능 요약: 관심있는 주식의 데이터의 과거데이터, 실시간 거래가, 예산내의 주식 포트폴리오 매수가격과 갯수를 계산할 수 있다.

## 4. Key Features
- ### Backend
  - KIS Open API 연동 (토큰 자동 발급 및 갱신)
  - 분봉/일봉/주봉/월봉 차트 데이터 제공
  - 시가총액 Top20 종목 리스트 제공
  - ETF 괴리율 및 수급 데이터 수집
  - WebSocket 기반 실시간 체결강도 반영
- ### Frontend
  - 대시보드
    - 캔들차트 + 거래량 오버레이
    - ETF / 개별 주식 종목 리스트
    - 실시간 체결강도 (120% 이상 필터링)    
  - 모니터링 페이지
    - 시가총액 Top20 실시간 흐름
    - ETF 괴리율 확인
## 5. Data Flow
```
사용자가 차트 데이터 요청
    → JWT 포함 요청
    → 백엔드에서 토큰 갱신 및 API 호출
    → KIS API에서 데이터 수신
    → JSON 반환
    → 프론트엔드에서 lightweight-charts로 차트 렌더링
```
```  
  실시간 데이터 구독
    → WebSocket 연결
    → 체결강도/수급 데이터 스트리밍
    → 대시보드 실시간 반영
```
## 6. DB Structure
`🚧 이미지 추가예정`

## 7. Project Manual

## 8. CI/CD

## 9. Trouble Shooting


<img src="https://capsule-render.vercel.app/api?type=waving&color=BDBDC8&height=150&section=footer" />
