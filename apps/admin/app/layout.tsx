import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Dictum Admin",
  description: "Admin interface for the Dictum blog control plane.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
