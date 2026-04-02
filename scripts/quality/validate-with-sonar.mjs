import { existsSync, readFileSync } from "node:fs";
import { homedir } from "node:os";
import { join } from "node:path";
import process from "node:process";
import {
  commandExists,
  info,
  runCapturedCommand,
  runCheckedCommand,
  sleep,
} from "../lib/cli.mjs";

const pnpmCommand = process.platform === "win32" ? "pnpm.cmd" : "pnpm";
const requiredSonarEnv = ["SONAR_HOST_URL", "SONAR_TOKEN"];
const localSonarHostUrl = "http://localhost:9000";
const localConfigFile = resolveLocalConfigFile();
const shouldAutoStartLocalServer = parseBooleanEnv(process.env.DICTUM_SONAR_AUTO_START);
const shouldStopLocalServerAfterValidation = parseBooleanEnv(
  process.env.DICTUM_SONAR_STOP_AFTER_VALIDATE,
);
const sonarScannerArgs = process.argv.slice(2);

for (const phase of [
  { label: "Generate API clients", args: ["generate:api"] },
  { label: "Lint web workspace", args: ["lint:web"] },
  { label: "Typecheck web workspace", args: ["typecheck:web"] },
  { label: "Verify API module", args: ["verify:api"] },
]) {
  runPnpmPhase(phase.label, phase.args);
}

const sonarConfig = await resolveSonarConfig();

if (!sonarConfig) {
  warnSkippedSonar(
    [
      "SonarQube is not configured locally.",
      "Set SONAR_HOST_URL and SONAR_TOKEN or configure ~/.config/dictum/sonarqube.json if you want to enable server analysis later.",
    ].join(" "),
  );
  process.exit(0);
}

const localServerReady = await ensureLocalServerReady(sonarConfig);

if (!localServerReady.ready) {
  process.exit(0);
}

try {
  runPnpmPhase(
    "Run SonarQube analysis",
    ["exec", "sonar", "-Dsonar.qualitygate.wait=true", ...sonarScannerArgs],
    {
      ...process.env,
      SONAR_HOST_URL: sonarConfig.sonarHostUrl,
      SONAR_TOKEN: sonarConfig.sonarToken,
    },
  );
} finally {
  stopLocalServerIfStartedByThisRun(localServerReady);
}

function runPnpmPhase(label, args, env = process.env) {
  info(`==> ${label}`);

  const result = runCapturedCommand(pnpmCommand, args, { env });

  const output = [result.stdout, result.stderr].filter(Boolean).join("");

  if (result.status !== 0) {
    if (output.trim()) {
      process.stderr.write(output);
    }

    process.exit(result.status ?? 1);
  }

  const diagnostics = extractRelevantOutput(output);

  if (diagnostics) {
    process.stdout.write(`${diagnostics}\n`);
  }

  info(`✓ ${label}`);
}

function extractRelevantOutput(output) {
  const lines = output
    .split(/\r?\n/u)
    .map((line) => line.trimEnd())
    .filter(Boolean);

  const relevantLines = lines.filter((line) =>
    /\b(?:WARN|WARNING|ERROR|FAIL|FAILURE)\b/u.test(line) ||
    /QUALITY GATE STATUS/u.test(line) ||
    /SonarScanner Engine completed successfully/u.test(line),
  );

  return compressRepeatedLines(relevantLines).join("\n");
}

function compressRepeatedLines(lines) {
  const compressedLines = [];
  let previousLine = null;
  let repeatCount = 0;

  for (const line of lines) {
    if (line === previousLine) {
      repeatCount += 1;
      continue;
    }

    if (previousLine !== null) {
      compressedLines.push(formatRepeatedLine(previousLine, repeatCount));
    }

    previousLine = line;
    repeatCount = 1;
  }

  if (previousLine !== null) {
    compressedLines.push(formatRepeatedLine(previousLine, repeatCount));
  }

  return compressedLines;
}

function formatRepeatedLine(line, repeatCount) {
  if (repeatCount === 1) {
    return line;
  }

  return `${line} (repeated ${repeatCount}x)`;
}

async function resolveSonarConfig() {
  const envConfig = readEnvConfig();

  if (envConfig) {
    return envConfig;
  }

  const localConfig = readLocalConfig();

  if (localConfig) {
    return localConfig;
  }

  return null;
}

async function ensureLocalServerReady(sonarConfig) {
  if (sonarConfig.sonarHostUrl !== localSonarHostUrl) {
    return { ready: true, startedByThisRun: false };
  }

  if (await isServerReady()) {
    return { ready: true, startedByThisRun: false };
  }

  if (!shouldAutoStartLocalServer) {
    warnSkippedSonar(
      [
        "Local SonarQube is configured for http://localhost:9000, but the server is not running.",
        "Start it with `pnpm sonar:start` or rerun with `DICTUM_SONAR_AUTO_START=true` if you want validation to start Docker automatically.",
      ].join(" "),
    );
    return { ready: false, startedByThisRun: false };
  }

  if (!commandExists("docker", ["--version"])) {
    warnSkippedSonar(
      "Local SonarQube is configured for http://localhost:9000, but Docker is not available to start it automatically.",
    );
    return { ready: false, startedByThisRun: false };
  }

  info("Local SonarQube is not running. Attempting to start it with Docker...");
  runCommand("docker", ["compose", "up", "-d"]);

  if (await waitForServerReady()) {
    return { ready: true, startedByThisRun: true };
  }

  warnSkippedSonar("Local SonarQube could not be started automatically.");
  return { ready: false, startedByThisRun: true };
}

function runCommand(command, args) {
  runCheckedCommand(command, args);
}

function stopLocalServerIfStartedByThisRun(localServerState) {
  if (!localServerState.startedByThisRun || !shouldStopLocalServerAfterValidation) {
    return;
  }

  info("Stopping the local SonarQube Docker stack started for this validation run...");
  runCommand("docker", ["compose", "down"]);
}

function parseBooleanEnv(value) {
  if (!value) {
    return false;
  }

  return ["1", "true", "yes", "on"].includes(value.trim().toLowerCase());
}

function readEnvConfig() {
  const missingEnv = requiredSonarEnv.filter((name) => !process.env[name]?.trim());

  if (missingEnv.length > 0) {
    return null;
  }

  return {
    sonarHostUrl: process.env.SONAR_HOST_URL.trim(),
    sonarToken: process.env.SONAR_TOKEN.trim(),
  };
}

function readLocalConfig() {
  if (!existsSync(localConfigFile)) {
    return null;
  }

  try {
    const config = JSON.parse(readFileSync(localConfigFile, "utf8"));

    if (config?.sonarHostUrl?.trim() && config?.sonarToken?.trim()) {
      return {
        sonarHostUrl: config.sonarHostUrl.trim(),
        sonarToken: config.sonarToken.trim(),
      };
    }
  } catch {
    warnSkippedSonar(
      `The local SonarQube config at ${localConfigFile} could not be read. It will be ignored for this run.`,
    );
  }

  return null;
}

function resolveLocalConfigFile() {
  const baseDir =
    process.env.XDG_CONFIG_HOME?.trim() || join(homedir(), ".config");

  return join(baseDir, "dictum", "sonarqube.json");
}

async function waitForServerReady() {
  const maxAttempts = 30;

  for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
    if (await isServerReady()) {
      return true;
    }

    await sleep(3000);
  }

  return false;
}

async function isServerReady() {
  try {
    const response = await fetch(`${localSonarHostUrl}/api/system/status`);

    if (!response.ok) {
      return false;
    }

    const payload = await response.json();
    return payload.status === "UP";
  } catch {
    return false;
  }
}

function warnSkippedSonar(reason) {
  console.warn(
    [
      "WARNING: SonarQube analysis was skipped.",
      reason,
      "The standard repository validations still ran successfully.",
      "This run did not use the SonarQube server analysis feature.",
    ].join(" "),
  );
}
