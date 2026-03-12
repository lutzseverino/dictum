import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  transpilePackages: ["@dictum/rendering", "@dictum/site-kit"],
};

export default nextConfig;
