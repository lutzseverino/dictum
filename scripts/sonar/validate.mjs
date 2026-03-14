import { spawnSync } from "node:child_process";

const pnpmCommand = process.platform === "win32" ? "pnpm.cmd" : "pnpm";
const requiredSonarEnv = ["SONAR_HOST_URL", "SONAR_TOKEN"];

for (const args of [
  ["generate:api"],
  ["lint:web"],
  ["typecheck:web"],
  ["verify:api"],
]) {
  runPnpm(args);
}

const missingEnv = requiredSonarEnv.filter((name) => !process.env[name]?.trim());

if (missingEnv.length > 0) {
  console.warn(
    [
      "WARNING: SonarQube analysis was skipped because the Sonar environment is not configured.",
      `Missing: ${missingEnv.join(", ")}.`,
      "The standard repository validations still ran successfully.",
    ].join(" "),
  );
  process.exit(0);
}

runPnpm(["exec", "sonar", "-Dsonar.qualitygate.wait=true"]);

function runPnpm(args) {
  const result = spawnSync(pnpmCommand, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "inherit",
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}
