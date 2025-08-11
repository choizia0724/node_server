import oracledb from "oracledb";

export default async function connectDB() {
  try {
    // TNS_ADMIN 환경변수가 자동으로 wallet 내 tnsnames.ora 위치를 가리킵니다.
    const connection = await oracledb.getConnection({
      user: process.env.DB_USER, // .env 파일에서 DB 사용자 이름을 가져옵니다.
      password: process.env.DB_PASSWORD, // .env 파일에서 DB 비밀번호를 가져옵니다.
      connectString:
        "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.ap-chuncheon-1.oraclecloud.com))(connect_data=(service_name=g84857d7fd71aaa_pkodaz6a1r6ul85c_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
      walletLocation: "/app/wallet",
    });

    console.log("DB 연결 성공!");

    return connection;
  } catch (err) {
    console.error("DB 연결 에러:", err);
  }
}
