import dotenv from "dotenv";
dotenv.config();

export const configFile = {
  username: process.env.PG_USER,
  password: process.env.PG_PASSWORD,
  database: process.env.PG_DB,
  host: process.env.PG_HOST,
  port: process.env.PG_PORT,
  dialect: "postgres",
};
