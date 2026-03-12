import type { MetadataRoute } from "next";

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Dictum Admin",
    short_name: "Dictum",
    description: "Phone-first control shell for the Dictum platform.",
    start_url: "/",
    display: "standalone",
    background_color: "#f7f4ec",
    theme_color: "#0369a1",
    icons: [
      {
        src: "/favicon.ico",
        sizes: "any",
        type: "image/x-icon",
      },
    ],
  };
}
