#!/usr/bin/env node

const { spawn } = require('node:child_process');
const readline = require('node:readline');

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function quoteCmdArg(arg) {
  if (arg === '') {
    return '""';
  }
  if (/[\s"]/g.test(arg)) {
    return `"${arg.replace(/"/g, '\\"')}"`;
  }
  return arg;
}

function spawnServer(commandLine, env = {}) {
  const proc = spawn(process.env.ComSpec || 'cmd.exe', ['/d', '/s', '/c', commandLine], {
    stdio: ['pipe', 'pipe', 'pipe'],
    env: { ...process.env, ...env },
    windowsHide: true,
  });
  return proc;
}

function createMcpClient(proc, label) {
  let nextId = 1;
  const pending = new Map();

  const rl = readline.createInterface({ input: proc.stdout });

  rl.on('line', (line) => {
    const trimmed = line.trim();
    if (!trimmed) {
      return;
    }

    let msg;
    try {
      msg = JSON.parse(trimmed);
    } catch {
      process.stdout.write(`[${label}] non-json stdout: ${trimmed}\n`);
      return;
    }

    if (msg.id !== undefined) {
      const entry = pending.get(msg.id);
      if (!entry) {
        process.stdout.write(`[${label}] unexpected response id=${msg.id}\n`);
        return;
      }
      pending.delete(msg.id);
      if (msg.error) {
        entry.reject(new Error(JSON.stringify(msg.error)));
      } else {
        entry.resolve(msg.result);
      }
      return;
    }

    if (msg.method) {
      process.stdout.write(`[${label}] notification: ${msg.method}\n`);
    } else {
      process.stdout.write(`[${label}] event: ${trimmed}\n`);
    }
  });

  proc.stderr.on('data', (chunk) => {
    const text = chunk.toString('utf8').trimEnd();
    if (text) {
      process.stderr.write(`[${label}:stderr] ${text}\n`);
    }
  });

  proc.on('exit', (code, signal) => {
    for (const [, entry] of pending) {
      entry.reject(new Error(`server exited before response (code=${code}, signal=${signal})`));
    }
    pending.clear();
  });

  function request(method, params = {}) {
    const id = nextId++;
    const payload = {
      jsonrpc: '2.0',
      id,
      method,
      params,
    };
    const raw = `${JSON.stringify(payload)}\n`;
    proc.stdin.write(raw);
    return new Promise((resolve, reject) => {
      pending.set(id, { resolve, reject });
    });
  }

  function notify(method, params = {}) {
    const payload = {
      jsonrpc: '2.0',
      method,
      params,
    };
    proc.stdin.write(`${JSON.stringify(payload)}\n`);
  }

  async function close() {
    rl.close();
    proc.stdin.end();
    await sleep(250);
    if (!proc.killed) {
      proc.kill('SIGTERM');
    }
  }

  return { request, notify, close };
}

async function runSequentialThinkingSmoke() {
  const label = 'sequential-thinking';
  const proc = spawnServer(`npx -y @modelcontextprotocol/server-sequential-thinking`);
  const client = createMcpClient(proc, label);
  try {
    console.log(`== ${label} ==`);
    await client.request('initialize', {
      protocolVersion: '2024-11-05',
      clientInfo: { name: 'senku-mcp-smoke', version: '1.0.0' },
      capabilities: {},
    });
    client.notify('notifications/initialized', {});

    const tools = await client.request('tools/list', {});
    console.log(`[${label}] tools: ${tools.tools.map((t) => t.name).join(', ')}`);

    const toolName = tools.tools.some((t) => t.name === 'sequential_thinking')
      ? 'sequential_thinking'
      : tools.tools[0]?.name;
    if (!toolName) {
      throw new Error('no tools exposed by sequential-thinking server');
    }

    const result = await client.request('tools/call', {
      name: toolName,
      arguments: {
        thought: 'Smoke test: verify the sequential-thinking MCP server works.',
        nextThoughtNeeded: false,
        thoughtNumber: 1,
        totalThoughts: 1,
      },
    });

    console.log(`[${label}] tool=${toolName}`);
    console.log(`[${label}] result=${JSON.stringify(result, null, 2)}`);
  } finally {
    await client.close();
  }
}

async function runPuppeteerSmoke() {
  const label = 'puppeteer';
  const proc = spawnServer(`npx -y @modelcontextprotocol/server-puppeteer`);
  const client = createMcpClient(proc, label);
  try {
    console.log(`== ${label} ==`);
    await client.request('initialize', {
      protocolVersion: '2024-11-05',
      clientInfo: { name: 'senku-mcp-smoke', version: '1.0.0' },
      capabilities: {},
    });
    client.notify('notifications/initialized', {});

    const tools = await client.request('tools/list', {});
    console.log(`[${label}] tools: ${tools.tools.map((t) => t.name).join(', ')}`);

    const toolNames = new Set(tools.tools.map((t) => t.name));
    if (!toolNames.has('puppeteer_navigate')) {
      throw new Error('puppeteer_navigate tool not exposed');
    }
    if (!toolNames.has('puppeteer_screenshot')) {
      throw new Error('puppeteer_screenshot tool not exposed');
    }

    const pageHtml = encodeURIComponent('<!doctype html><html><head><title>Senku MCP Smoke</title></head><body><h1>Senku MCP Smoke</h1><p>Browser tool verified.</p></body></html>');
    const dataUrl = `data:text/html;charset=utf-8,${pageHtml}`;

    const nav = await client.request('tools/call', {
      name: 'puppeteer_navigate',
      arguments: { url: dataUrl },
    });

    const shot = await client.request('tools/call', {
      name: 'puppeteer_screenshot',
      arguments: { name: 'senku-mcp-smoke' },
    });

    console.log(`[${label}] navigate=${JSON.stringify(nav, null, 2)}`);
    console.log(`[${label}] screenshot=${JSON.stringify(shot, null, 2)}`);
  } finally {
    await client.close();
  }
}

async function runGitSmoke() {
  const label = 'git';
  const repo = 'C:\\Users\\tateb\\Documents\\senku_local_testing_bundle_20260410';
  const proc = spawnServer(`uvx mcp-server-git --repository ${quoteCmdArg(repo)}`);
  const client = createMcpClient(proc, label);
  try {
    console.log(`== ${label} ==`);
    await client.request('initialize', {
      protocolVersion: '2024-11-05',
      clientInfo: { name: 'senku-mcp-smoke', version: '1.0.0' },
      capabilities: {},
    });
    client.notify('notifications/initialized', {});

    const tools = await client.request('tools/list', {});
    console.log(`[${label}] tools: ${tools.tools.map((t) => t.name).join(', ')}`);

    const toolNames = new Set(tools.tools.map((t) => t.name));
    const statusTool = tools.tools.find((t) => /status/i.test(t.name) || /summary/i.test(t.name) || /log/i.test(t.name))?.name;
    if (statusTool) {
      const result = await client.request('tools/call', {
        name: statusTool,
        arguments: {},
      });
      console.log(`[${label}] tool=${statusTool}`);
      console.log(`[${label}] result=${JSON.stringify(result, null, 2)}`);
    } else if (toolNames.size === 0) {
      throw new Error('no tools exposed by git server');
    }
  } finally {
    await client.close();
  }
}

async function main() {
  await runSequentialThinkingSmoke();
  await runPuppeteerSmoke();
  await runGitSmoke();
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
