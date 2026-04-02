import { existsSync, readFileSync, statSync } from "node:fs";
import { readdir, stat } from "node:fs/promises";
import { homedir } from "node:os";
import { join, resolve } from "node:path";
import process from "node:process";
import { spawn, spawnSync } from "node:child_process";

const localSonarHostUrl = "http://localhost:9000";
const localConfigFile = resolveLocalConfigFile();
const targetArguments = process.argv.slice(2);
const shouldAutoStartLocalServer = parseBooleanEnv(process.env.DICTUM_SONAR_AUTO_START);

const sonarConfig = readEnvConfig() ?? readLocalConfig();

if (!sonarConfig) {
  console.error(
    [
      "WARNING: Local SonarQube diagnostics are unavailable.",
      "Set SONAR_HOST_URL and SONAR_TOKEN or configure ~/.config/dictum/sonarqube.json first.",
      "This run did not use the IDE-backed diagnostics feature.",
    ].join(" "),
  );
  process.exit(1);
}

const localServerReady = await ensureLocalServerReady(sonarConfig);

if (!localServerReady) {
  process.exit(1);
}

const idePort = await discoverIdePort();

if (!idePort) {
  console.error(
    [
      "WARNING: Local SonarQube diagnostics are unavailable.",
      "Could not detect a running SonarQube for IDE bridge.",
      "Open this repository in VS Code with the SonarQube for IDE extension enabled, then rerun the command.",
      "This run did not use the IDE-backed diagnostics feature.",
    ].join(" "),
  );
  process.exit(1);
}

const filePaths = await resolveTargetFiles(targetArguments);

if (filePaths.length === 0) {
  console.error("No files matched the requested target.");
  process.exit(1);
}

const findings = await analyzeFilesWithMcp({
  filePaths,
  idePort,
  sonarHostUrl: translateHostUrlForContainer(sonarConfig.sonarHostUrl),
  sonarToken: sonarConfig.sonarToken,
});

if (findings.length === 0) {
  console.log("No SonarQube for IDE findings.");
  process.exit(0);
}

for (const finding of findings) {
  const filePath = finding.filePath ?? "<unknown>";
  const startLine = finding.textRange?.startLine ?? "?";
  const endLine = finding.textRange?.endLine ?? startLine;
  const rangeLabel = startLine === endLine ? `${startLine}` : `${startLine}-${endLine}`;
  const severity = finding.severity ?? "UNKNOWN";
  const message = finding.message ?? "No message";

  console.log(`${severity} ${filePath}:${rangeLabel} ${message}`);
}

function readEnvConfig() {
  if (!process.env.SONAR_HOST_URL?.trim() || !process.env.SONAR_TOKEN?.trim()) {
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
    return null;
  }

  return null;
}

function resolveLocalConfigFile() {
  const baseDir =
    process.env.XDG_CONFIG_HOME?.trim() || join(homedir(), ".config");

  return join(baseDir, "dictum", "sonarqube.json");
}

async function ensureLocalServerReady(sonarConfig) {
  if (sonarConfig.sonarHostUrl !== localSonarHostUrl) {
    return true;
  }

  if (await isServerReady()) {
    return true;
  }

  if (!shouldAutoStartLocalServer) {
    console.error(
      [
        "WARNING: Local SonarQube diagnostics are unavailable.",
        "Local SonarQube is configured for http://localhost:9000, but the server is not running.",
        "Start it with `pnpm sonar:start` or rerun with `DICTUM_SONAR_AUTO_START=true` if you want diagnostics to start Docker automatically.",
        "This run did not use the IDE-backed diagnostics feature.",
      ].join(" "),
    );
    return false;
  }

  if (!commandExists("docker", ["--version"])) {
    console.error(
      [
        "WARNING: Local SonarQube diagnostics are unavailable.",
        "Local SonarQube is configured for http://localhost:9000, but Docker is not available to start it automatically.",
        "This run did not use the IDE-backed diagnostics feature.",
      ].join(" "),
    );
    return false;
  }

  console.log("Starting local SonarQube with Docker...");

  if (!runCheckedCommand("docker", ["compose", "up", "-d"])) {
    return false;
  }

  for (let attempt = 0; attempt < 30; attempt += 1) {
    if (await isServerReady()) {
      return true;
    }

    await sleep(3000);
  }

  console.error("Local SonarQube did not become reachable at http://localhost:9000 in time.");
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

function translateHostUrlForContainer(sonarHostUrl) {
  return sonarHostUrl
    .replace("://localhost", "://host.docker.internal")
    .replace("://127.0.0.1", "://host.docker.internal");
}

async function discoverIdePort() {
  const configuredPort = process.env.SONARQUBE_IDE_PORT?.trim();

  if (configuredPort) {
    return configuredPort;
  }

  const livePorts = discoverLiveIdePorts();

  if (livePorts.length === 0) {
    return null;
  }

  const portsFromLogs = discoverRecentPortsFromLogs();

  for (const port of [...portsFromLogs].reverse()) {
    if (livePorts.includes(port)) {
      return String(port);
    }
  }

  return String(livePorts[livePorts.length - 1]);
}

function discoverLiveIdePorts() {
  const result = spawnSync("lsof", ["-nP", "-iTCP", "-sTCP:LISTEN"], {
    cwd: process.cwd(),
    env: process.env,
    encoding: "utf8",
  });

  if (result.status !== 0) {
    return [];
  }

  const candidatePorts = result.stdout
    .split(/\r?\n/u)
    .filter((line) => line.includes("127.0.0.1:6412"))
    .flatMap((line) => {
      const match = line.match(/127\.0\.0\.1:(6412\d)\s+\(LISTEN\)/u);
      return match ? [Number.parseInt(match[1], 10)] : [];
    });

  return [...new Set(candidatePorts)].sort((left, right) => left - right);
}

function discoverRecentPortsFromLogs() {
  const logsRoot = resolve(
    homedir(),
    "Library",
    "Application Support",
    "Code",
    "logs",
  );

  if (!existsSync(logsRoot)) {
    return [];
  }

  const portEntries = [];

  for (const logFile of findSonarIdeLogFiles(logsRoot)) {
    let stats;

    try {
      stats = readFileSync(logFile, "utf8");
    } catch {
      continue;
    }

    const matches = [...stats.matchAll(/Started embedded server on port (6412\d)/gu)];

    if (matches.length === 0) {
      continue;
    }

    const newestMatch = matches[matches.length - 1];
    const port = Number.parseInt(newestMatch[1], 10);
    const modifiedAt = statSync(logFile).mtimeMs;

    portEntries.push({
      port,
      sortKey: `${modifiedAt}:${logFile}`,
    });
  }

  return portEntries
    .sort((left, right) => left.sortKey.localeCompare(right.sortKey))
    .map((entry) => entry.port);
}

function findSonarIdeLogFiles(root) {
  const result = spawnSync(
    "find",
    [root, "-name", "SonarQube for IDE.log", "-print"],
    {
      cwd: process.cwd(),
      env: process.env,
      encoding: "utf8",
    },
  );

  if (result.status !== 0) {
    return [];
  }

  return result.stdout
    .split(/\r?\n/u)
    .map((line) => line.trim())
    .filter(Boolean);
}

async function resolveTargetFiles(targets) {
  const rawTargets = targets.length === 0 ? [process.cwd()] : targets;
  const resolvedFiles = [];

  for (const target of rawTargets) {
    const absoluteTarget = resolve(process.cwd(), target);

    if (!existsSync(absoluteTarget)) {
      continue;
    }

    const targetStat = await stat(absoluteTarget);

    if (targetStat.isFile()) {
      resolvedFiles.push(absoluteTarget);
      continue;
    }

    if (targetStat.isDirectory()) {
      resolvedFiles.push(...(await walkFiles(absoluteTarget)));
    }
  }

  return [...new Set(resolvedFiles)];
}

async function walkFiles(root) {
  const entries = await readdir(root, { withFileTypes: true });
  const files = [];

  for (const entry of entries) {
    const entryPath = join(root, entry.name);

    if (entry.isDirectory()) {
      if (
        entry.name === ".git" ||
        entry.name === "node_modules" ||
        entry.name === ".next" ||
        entry.name === "target" ||
        entry.name === "dist"
      ) {
        continue;
      }

      files.push(...(await walkFiles(entryPath)));
      continue;
    }

    files.push(entryPath);
  }

  return files;
}

async function analyzeFilesWithMcp({ filePaths, idePort, sonarHostUrl, sonarToken }) {
  const dockerArgs = [
    "run",
    "-i",
    "--rm",
    "--init",
    "-e",
    "SONARQUBE_TOKEN",
    "-e",
    "SONARQUBE_URL",
    "-e",
    "SONARQUBE_IDE_PORT",
    "mcp/sonarqube",
  ];

  const child = spawn("docker", dockerArgs, {
    cwd: process.cwd(),
    env: {
      ...process.env,
      SONARQUBE_TOKEN: sonarToken,
      SONARQUBE_URL: sonarHostUrl,
      SONARQUBE_IDE_PORT: idePort,
    },
    stdio: ["pipe", "pipe", "pipe"],
  });

  const stderrChunks = [];
  child.stderr.on("data", (chunk) => {
    stderrChunks.push(chunk.toString("utf8"));
  });

  await waitForReadyLog(stderrChunks, child);

  const stdoutParser = createMcpParser(child.stdout);
  let nextId = 1;

  const initializeResponse = await sendMcpRequest(child.stdin, stdoutParser, {
    jsonrpc: "2.0",
    id: nextId,
    method: "initialize",
    params: {
      protocolVersion: "2024-11-05",
      capabilities: {},
      clientInfo: {
        name: "dictum-sonarqube-ide",
        version: "1.0",
      },
    },
  });

  nextId += 1;

  if (initializeResponse.error) {
    throw new Error(`MCP initialize failed: ${JSON.stringify(initializeResponse.error)}`);
  }

  sendNotification(child.stdin, {
    jsonrpc: "2.0",
    method: "notifications/initialized",
    params: {},
  });

  const toolsResponse = await sendMcpRequest(child.stdin, stdoutParser, {
    jsonrpc: "2.0",
    id: nextId,
    method: "tools/list",
    params: {},
  });

  nextId += 1;

  const tools = toolsResponse.result?.tools ?? [];
  const hasAnalyzeFileList = tools.some((tool) => tool.name === "analyze_file_list");

  if (!hasAnalyzeFileList) {
    throw new Error(
      "The SonarQube MCP server did not expose analyze_file_list. Make sure SonarQube for IDE is running and connected.",
    );
  }

  const analyzeResponse = await sendMcpRequest(child.stdin, stdoutParser, {
    jsonrpc: "2.0",
    id: nextId,
    method: "tools/call",
    params: {
      name: "analyze_file_list",
      arguments: {
        file_absolute_paths: filePaths,
      },
    },
  });

  child.stdin.end();
  child.kill();

  if (analyzeResponse.error) {
    throw new Error(`analyze_file_list failed: ${JSON.stringify(analyzeResponse.error)}`);
  }

  if (parseBooleanEnv(process.env.DICTUM_SONAR_IDE_DEBUG)) {
    console.error(JSON.stringify(analyzeResponse, null, 2));
  }

  return analyzeResponse.result?.structuredContent?.findings ?? [];
}

function waitForReadyLog(stderrChunks, child) {
  return new Promise((resolvePromise, rejectPromise) => {
    const startTime = Date.now();
    const interval = setInterval(() => {
      const stderr = stderrChunks.join("");

      if (stderr.includes("SonarQube MCP Server Started:")) {
        clearInterval(interval);
        resolvePromise();
        return;
      }

      if (child.exitCode !== null) {
        clearInterval(interval);
        rejectPromise(new Error(stderr || "SonarQube MCP server exited before becoming ready."));
        return;
      }

      if (Date.now() - startTime > 15000) {
        clearInterval(interval);
        rejectPromise(new Error(stderr || "Timed out waiting for SonarQube MCP server startup."));
      }
    }, 100);
  });
}

function createMcpParser(stdout) {
  let buffer = "";
  const pendingResolvers = [];

  stdout.on("data", (chunk) => {
    buffer += chunk.toString("utf8");
    drainBuffer();
  });

  stdout.on("end", () => {
    while (pendingResolvers.length > 0) {
      const nextResolver = pendingResolvers.shift();
      nextResolver?.reject(new Error("MCP stdout closed unexpectedly."));
    }
  });

  function waitForMessage() {
    return new Promise((resolvePromise, rejectPromise) => {
      pendingResolvers.push({ resolve: resolvePromise, reject: rejectPromise });
      drainBuffer();
    });
  }

  function drainBuffer() {
    while (true) {
      const newlineIndex = buffer.indexOf("\n");

      if (newlineIndex === -1) {
        return;
      }

      const rawLine = buffer.slice(0, newlineIndex).trim();
      buffer = buffer.slice(newlineIndex + 1);

      if (!rawLine) {
        continue;
      }

      if (!rawLine.startsWith("{")) {
        return;
      }

      const nextResolver = pendingResolvers.shift();

      if (!nextResolver) {
        continue;
      }

      try {
        nextResolver.resolve(JSON.parse(rawLine));
      } catch (error) {
        nextResolver.reject(error);
      }
    }
  }

  return { waitForMessage };
}

async function sendMcpRequest(stdin, parser, request) {
  writeMessage(stdin, request);

  while (true) {
    const message = await parser.waitForMessage();

    if (message.id === request.id) {
      return message;
    }
  }
}

function sendNotification(stdin, notification) {
  writeMessage(stdin, notification);
}

function writeMessage(stdin, payload) {
  stdin.write(`${JSON.stringify(payload)}\n`);
}

function commandExists(command, args) {
  const result = spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "ignore",
  });

  return result.status === 0;
}

function runCheckedCommand(command, args) {
  const result = spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "inherit",
  });

  return result.status === 0;
}

function sleep(ms) {
  return new Promise((resolvePromise) => setTimeout(resolvePromise, ms));
}

function parseBooleanEnv(value) {
  if (!value) {
    return false;
  }

  return ["1", "true", "yes", "on"].includes(value.trim().toLowerCase());
}
