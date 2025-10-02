import type { NextConfig } from "next";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE;

const nextConfig: NextConfig = {
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: true },
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${API_BASE}/api/:path*`, // 백엔드로 프록시
      },
    ];
  },
};

export default nextConfig;
