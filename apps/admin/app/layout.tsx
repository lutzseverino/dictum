import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Dictum Admin",
  description: "Empty admin shell reserved for future work.",
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
