import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: true },
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://129.154.194.75:30020/:path*", // 백엔드 주소
      },
    ];
  },
};

export default nextConfig;
