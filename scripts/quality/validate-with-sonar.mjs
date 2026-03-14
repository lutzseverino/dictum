import { chmodSync, existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { homedir } from "node:os";
import { dirname, join } from "node:path";
import { createInterface } from "node:readline/promises";
import { spawnSync } from "node:child_process";
import process from "node:process";

const pnpmCommand = process.platform === "win32" ? "pnpm.cmd" : "pnpm";
const requiredSonarEnv = ["SONAR_HOST_URL", "SONAR_TOKEN"];
const localSonarHostUrl = "http://localhost:9000";
const localConfigFile = resolveLocalConfigFile();

for (const args of [
  ["generate:api"],
  ["lint:web"],
  ["typecheck:web"],
  ["verify:api"],
]) {
  runPnpm(args);
}

const sonarConfig = await resolveSonarConfig();

if (!sonarConfig) {
  warnSkippedSonar(
    "SonarQube is not configured locally. If you want to enable it later, run `pnpm sonar:validate` in an interactive terminal.",
  );
  process.exit(0);
}

const localServerReady = await ensureLocalServerReady(sonarConfig);

if (!localServerReady) {
  process.exit(0);
}

runPnpm(["exec", "sonar", "-Dsonar.qualitygate.wait=true"], {
  ...process.env,
  SONAR_HOST_URL: sonarConfig.sonarHostUrl,
  SONAR_TOKEN: sonarConfig.sonarToken,
});

async function resolveSonarConfig() {
  const envConfig = readEnvConfig();

  if (envConfig) {
    return envConfig;
  }

  const localConfig = readLocalConfig();

  if (localConfig) {
    return localConfig;
  }

  if (!isInteractiveTerminal()) {
    return null;
  }

  if (!commandExists("docker", ["--version"])) {
    warnSkippedSonar(
      "Docker is not available in this shell, so local SonarQube setup could not start automatically.",
    );
    return null;
  }

  const shouldSetup = await promptYesNo(
    "SonarQube is not configured. Do you want to try local Docker setup now? [Y/n] ",
  );

  if (!shouldSetup) {
    return null;
  }

  runCommand("docker", ["compose", "up", "-d"]);

  const isReady = await waitForServerReady();

  if (!isReady) {
    warnSkippedSonar(
      "Local SonarQube did not become reachable at http://localhost:9000 in time.",
    );
    return null;
  }

  const sonarToken = await promptForToken();

  if (!sonarToken) {
    warnSkippedSonar(
      "Local SonarQube is running, but no token was provided. Open http://localhost:9000, create a token, and rerun the command when you want SonarQube validation.",
    );
    return null;
  }

  const config = {
    sonarHostUrl: localSonarHostUrl,
    sonarToken,
  };

  saveLocalConfig(config);
  return config;
}

async function ensureLocalServerReady(sonarConfig) {
  if (sonarConfig.sonarHostUrl !== localSonarHostUrl) {
    return true;
  }

  if (await isServerReady()) {
    return true;
  }

  if (!commandExists("docker", ["--version"])) {
    warnSkippedSonar(
      "Local SonarQube is configured for http://localhost:9000, but Docker is not available to start it automatically.",
    );
    return false;
  }

  console.log("Local SonarQube is not running. Attempting to start it with Docker...");
  runCommand("docker", ["compose", "up", "-d"]);

  if (await waitForServerReady()) {
    return true;
  }

  warnSkippedSonar("Local SonarQube could not be started automatically.");
  return false;
}

function runPnpm(args, env = process.env) {
  const result = spawnSync(pnpmCommand, args, {
    cwd: process.cwd(),
    env,
    stdio: "inherit",
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

function runCommand(command, args) {
  const result = spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "inherit",
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
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

function saveLocalConfig(config) {
  mkdirSync(dirname(localConfigFile), { recursive: true });
  writeFileSync(localConfigFile, `${JSON.stringify(config, null, 2)}\n`, "utf8");

  if (process.platform !== "win32") {
    try {
      chmodSync(localConfigFile, 0o600);
    } catch {
      // Best effort only.
    }
  }
}

function resolveLocalConfigFile() {
  const baseDir =
    process.env.XDG_CONFIG_HOME?.trim() || join(homedir(), ".config");

  return join(baseDir, "dictum", "sonarqube.json");
}

function isInteractiveTerminal() {
  return Boolean(process.stdin.isTTY && process.stdout.isTTY);
}

function commandExists(command, args) {
  const result = spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "ignore",
  });

  return result.status === 0;
}

async function promptYesNo(message) {
  const response = await prompt(message);
  const normalized = response.trim().toLowerCase();

  return normalized === "" || normalized === "y" || normalized === "yes";
}

async function promptForToken() {
  return prompt(
    "Paste a SonarQube token for http://localhost:9000, or press Enter to skip for now: ",
  ).then((value) => value.trim());
}

async function prompt(message) {
  const readline = createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  try {
    return await readline.question(message);
  } finally {
    readline.close();
  }
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

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function warnSkippedSonar(reason) {
  console.warn(
    [
      "WARNING: SonarQube analysis was skipped.",
      reason,
      "The standard repository validations still ran successfully.",
    ].join(" "),
  );
}
