import { createInterface } from "node:readline/promises";
import { spawnSync } from "node:child_process";
import process from "node:process";

export function commandExists(command, args) {
  const result = spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "ignore",
  });

  return result.status === 0;
}

export function info(message) {
  console.log(message);
}

export function isInteractiveTerminal() {
  return Boolean(process.stdin.isTTY && process.stdout.isTTY);
}

export async function prompt(message) {
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

export async function promptYesNo(message) {
  const response = await prompt(message);
  const normalized = response.trim().toLowerCase();

  return normalized === "" || normalized === "y" || normalized === "yes";
}

export function runCheckedCommand(command, args, options = {}) {
  const result = spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    stdio: "inherit",
    ...options,
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

export function runCapturedCommand(command, args, options = {}) {
  return spawnSync(command, args, {
    cwd: process.cwd(),
    env: process.env,
    encoding: "utf8",
    stdio: "pipe",
    maxBuffer: 50 * 1024 * 1024,
    ...options,
  });
}

export function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
