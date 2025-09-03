import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/apis/:path*",
        destination: "http://129.154.194.75:32000/:path*", // 백엔드 주소
      },
    ];
  },
};

export default nextConfig;
