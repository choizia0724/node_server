import mongoose from "mongoose";

// MongoDB 연결 URI

const adminPassword =
  process.env.MONGODB_ADMIN_PASSWORD || "your_admin_password";
const MONGODB_URI = `mongodb://admin:${adminPassword}@mongodb:27017/stock_app_db`;

// MongoDB 연결 함수
const connectDB = async () => {
  try {
    await mongoose.connect(MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });
    console.log("MongoDB connected");
  } catch (err) {
    console.error("MongoDB connection error:", err);
    process.exit(1);
  }
};

export default connectDB;
