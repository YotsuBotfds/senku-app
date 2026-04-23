# **Technical Architecture for Autonomous Multi-Model Engineering Swarms: Orchestrating GPT-5.4, OpenCode GLM 5.1, Codex Spark 5.3, and Local Qwen 3.5 Scouts**

The transition of artificial intelligence from conversational assistance to autonomous engineering orchestration represents a fundamental shift in software development lifecycles. By the year 2026, the integration of high-reasoning frontier models with specialized open-weight implementations and local reconnaissance agents has enabled the creation of persistent swarms capable of managing complex codebases for extended durations with minimal human intervention.1 The core of this capability lies in the orchestration of diverse model architectures through the Codex Command Line Interface (CLI) and the OpenCode harness, supported by robust temporal scheduling mechanisms such as internal cron jobs and system-level task managers.4 This report provides an exhaustive technical analysis of the architectural requirements, communication protocols, and operational strategies necessary to deploy a "direction and go" multi-agent swarm.

## **The Strategic Orchestrator: GPT-5.4 and the Collapse of Workflow Complexity**

At the apex of the autonomous swarm sits the strategic orchestrator, typically powered by GPT-5.4. The primary function of this tier is task decomposition, intent classification, and high-level routing across specialized sub-agents.2 GPT-5.4 distinguishes itself from its predecessors through a 1-million-token context window and a significant improvement in "macro-reasoning," which allows it to maintain the state of a multi-hour project without the context fragmentation that characterized earlier systems.1

### **Workflow Simplification and Macro-Reasoning**

Empirical data from testing environments indicates that the reasoning capabilities of GPT-5.4 allow for the consolidation of fragmented micro-agent workflows into more cohesive "macro-agent" patterns.2 Previously, a complex engineering task might have required five or more specialized agents—each handling discrete steps like research, planning, implementation, testing, and review—to avoid context saturation or hallucinations.2 GPT-5.4 enables the collapsing of these nodes; for instance, a single "back-office agent" can now handle data extraction, sentiment analysis, and list generation in a single pass, weighing historical project context against live tool feedback without dropping crucial artifacts.2  
This collapse in orchestration overhead directly translates to higher reliability in long-running sessions. Every "handoff" between agents in a traditional swarm is a potential point of failure where context can be lost or distorted.7 By reducing the number of handoffs, GPT-5.4 minimizes cognitive distortion and ensures that the "direction" provided by the user remains the guiding principle throughout the autonomous session.2

### **Token Management and Strategic Fallbacks**

Despite the expanded context window, effective orchestration requires intelligent token management to sustain "hours on end" operations. GPT-5.4 achieves this by utilizing structured context objects rather than raw conversation history.7 The orchestrator maintains a typed state store that tracks extracted entities, project status, and active sub-agent tasks, passing only the relevant subsets to worker agents.7 For sub-tasks requiring less reasoning effort, the system can dynamically offload work to GPT-5.4 mini, which operates at approximately 30% of the standard quota, thereby extending the duration of the autonomous mission before hitting usage limits.1

## **The Implementation Tier: OpenCode and GLM 5.1 Harness Integration**

While GPT-5.4 handles strategic routing, the heavy implementation work—the actual generation and refactoring of code—is increasingly delegated to the GLM 5.1 model, specifically when utilized within the OpenCode harness.3 GLM 5.1 is an open-weight model with 754 billion parameters, designed specifically for high-horizon engineering tasks.3

### **Long-Horizon Autonomy in GLM 5.1**

The defining characteristic of GLM 5.1 is its ability to operate independently on a single task for durations exceeding eight hours.3 This capability is critical for scheduled overnight tasks where an agent must autonomously plan, execute, and self-improve without human loops.10 In the context of the requested swarm, GLM 5.1 acts as the "worker" that takes direction from GPT-5.4 and grinds through the technical implementation.11  
The OpenCode harness provides a streamlined environment for GLM 5.1, removing the unnecessary bloat found in more consumer-oriented CLIs.13 This allows the model to maximize its 1-million-token context window for code-related artifacts.10 Benchmarks such as SWE-bench Pro show that GLM 5.1 matches or exceeds the performance of GPT-5.4 on specific engineering metrics, making it a formidable implementation engine.9

| Metric | GPT-5.4 (Orchestrator) | GLM 5.1 (Worker) | Qwen 3.5 9b (Scout) |
| :---- | :---- | :---- | :---- |
| **Primary Role** | Task Routing & Strategy | Long-Horizon Coding | Recon & Verification |
| **Context Window** | 1,000,000 tokens | 1,000,000 tokens | 262,000 \- 1,000,000 tokens |
| **Best Harness** | Codex CLI / SDK | OpenCode | Local Ollama / vLLM |
| **Autonomous Duration** | High (Orchestration) | \> 8 Hours (Execution) | Low (Fast Checks) |
| **SWE-bench Pro** | High Tier | Matches GPT-5.4 | Mid Tier (for size) |

1

### **OpenCode Scripting and Parallelization**

A significant advantage of the OpenCode harness is its scriptability. Users can execute tasks via a CLI syntax such as opencode run "prompt" \--model provider/model-name, allowing for the automation of complex workflows through bash or python scripts.8 This facilitates the parallel execution of multiple GLM 5.1 sessions; an orchestrator can fan out a large-scale refactor to ten parallel GLM 5.1 agents, each working on a separate subsystem, and then aggregate the results once the implementation phase is complete.8

## **The Refinement Tier: Codex Spark 5.3 and Fast-Mode Iteration**

To achieve rapid iteration and A/B testing within the swarm, Codex Spark 5.3 serves as a "fast-tier" agent. While GPT-5.4 and GLM 5.1 focus on complex reasoning and long-duration execution, Spark is optimized for speed and the generation of multiple potential solutions for immediate evaluation.5

### **Speed vs. Proposal Quality**

In autonomous research harnesses, Codex Spark 5.3 is often used as the "researcher" that proposes rapid-fire changes to a codebase.5 Experimental data comparing Spark 5.3 to GPT-5.4 shows that Spark can be significantly faster—averaging 35 seconds less per call—but this speed comes at the cost of proposal acceptance rates.5 In a training optimization experiment, GPT-5.4 achieved a 67% acceptance rate for its proposed changes, while Spark 5.3 accepted only 17%, despite attempting nearly twice as many proposals.5  
This performance gap defines the role of Spark 5.3 in the swarm: it is best used for "brute-force" exploration or simple debugging tasks where speed is paramount.5 The orchestrator can delegate low-risk, high-velocity tasks to Spark 5.3, preserving the context and reasoning effort of the higher-tier models for architectural decisions.5

### **Toggle and Fast Modes**

The Codex CLI allows for easy switching to Spark or "Fast" modes through slash commands like /fast or /model.16 This allows the user or the orchestrator to dynamically adjust the reasoning effort of the session.16 For example, during the initial "direction" phase, GPT-5.4 can use high-reasoning mode to build a comprehensive plan, then switch to Spark 5.3 for the repetitive task of generating unit tests for 50 different modules.17

## **The Local Tier: Qwen 3.5 9b Scouts for Reconnaissance**

To minimize token costs and ensure that the orchestrator is always working with accurate local information, Qwen 3.5 9b "scout" agents are deployed locally.19 These scouts perform reconnaissance: they read files, run local tests, search through documentation, and provide summarized "intel" to the higher-tier models.21

### **Local Performance and Intelligence**

Qwen 3.5 9b represents a paradigm shift in small-model performance. In 2026-era benchmarks, the 9B model has been shown to crush 2024 frontier models on HumanEval, with coding performance jumping from 30.5% to 91.5%.19 This makes it more than capable of handling the initial exploration of a codebase or verifying that a build still passes after a refactor.19  
By running Qwen 3.5 9b locally (via Ollama or LM Studio), the swarm benefits from zero-cost, privacy-preserving inference for data-heavy tasks.20 The scouts can ingest a 262k token context window natively across all sizes, including the 9B model, which is essential for analyzing large portions of a repository simultaneously.14

### **Hardware and Resource Scaling**

Deploying Qwen scouts locally requires specific hardware considerations to maintain the speed necessary for real-time reconnaissance.

| Parameter | Qwen 3.5 9B (Recommended) | Qwen 3.5 2B (Edge) | Qwen 3.5 27B (High-End) |
| :---- | :---- | :---- | :---- |
| **Min VRAM (4-bit)** | 6–8 GB | 2–3 GB | 16–20 GB |
| **System RAM** | 16 GB | 8 GB | 32 GB |
| **Typical Speed** | 30 t/s (Ryzen AI Max) | 50+ t/s (Mobile) | 15 t/s (M4 Max) |
| **Primary Device** | Laptop with dGPU | Flagship Phone | Desktop Workstation |

14  
For optimal swarm performance, the Qwen scouts should be configured with a thinking/non-thinking toggle, allowing the orchestrator to request deeper analysis of a specific code path when the initial scout report is ambiguous.20

## **Temporal Orchestration: Cron Jobs and Scheduled Execution**

The "direction and go" capability requires the swarm to manage its own execution over time. This is achieved through a combination of internal session timers and external system-level schedulers.4

### **The Codex Internal /cron System**

The Codex CLI has introduced an internal /cron mechanism designed to periodically inject messages into the agent's input queue.4 This functions as a "heartbeat," preventing the agent from idling and allowing it to perform recurring background checks.4  
The internal cron behavior is governed by a single "turn lock" to ensure concurrency safety. Cron messages are injected as InputEvents into the session queue.4 To prevent a runaway backlog, the system uses a coalescing model: if a cron tick fires while a previous cron-generated task is still in progress, the new tick is dropped.4 This allows the agent to remain busy with its primary implementation work while still having a mechanism to "wake up" and report status or check for blockers at defined intervals.4

### **External Scheduling via Bash Harnesses and systemd**

For "hours on end" operations that must persist across system restarts or terminal closures, external scheduling is the preferred architectural choice.5  
A common implementation is the codex-autoresearch-harness, which wraps the codex exec command in a bash loop.5 This loop allows the agent to run iterative experiments, committing changes to git, running tests, and either keeping or reverting work based on the outcome.5 Because each iteration is a fresh call to codex exec, the system is resilient to crashes; a failure in one iteration does not kill the entire swarm.5  
For 24/7 reliability, the swarm orchestrator is often deployed as a systemd user service on a Linux or WSL2 environment.6 This ensures that the agent keeps running after the user logs out and can be configured with automated restart policies in the event of a process failure.6

Bash

\# Example systemd configuration for a persistent orchestrator  
\[Unit\]  
Description=Codex Swarm Orchestrator  
After=network.target

ExecStart=/usr/bin/node /usr/local/bin/swarm-manager.js \--direction "Refactor auth module"  
Restart=always  
RestartSec=10  
Environment=OPENAI\_API\_KEY=your\_key\_here

\[Install\]  
WantedBy=default.target

6

## **Artifact Persistence and Session Export**

To ensure that autonomous work is not lost when sessions end or when a local machine goes offline, several mechanisms exist for exporting plans and session histories.

### **Harness-Based Exporting**

The run-agent.sh orchestration engine automatically generates persistent artifacts for every run.12 Each execution produces a flat directory under .orchestrate/runs/agent-runs/\<run-id\>/ that contains:

* **report.md**: A high-level summary of the agent's findings and actions.12  
* **input.md**: The full prompt sent to the model for auditability.12  
* **files-touched.txt**: A log of every file modified during the session.12

### **Manual and Programmatic CLI Exports**

For users interacting directly with the Codex CLI, the /export command can be used to save the current conversation as a plain text file in the working directory. Additionally, the codex exec command supports a \-o \<file\> flag, which allows non-interactive runs to write their final output directly to a persistent file.39  
For more advanced needs, community tools like harness-recall can index and export sessions across multiple tools, including Codex, Claude Code, and Cursor, into Markdown or HTML formats for long-term storage or team review.

## **Connective Infrastructure: MCP and JSON-RPC 2.0**

The communication between GPT-5.4, GLM 5.1, and the Qwen scouts is facilitated by the Model Context Protocol (MCP) and the JSON-RPC 2.0 standard.30 This enables a modular system where specialized agents can expose their capabilities as services to the central orchestrator.32

### **Codex as an MCP Server**

Each agent in the swarm can operate in a dual capacity: as an MCP client that calls tools, and as an MCP server that exposes its core coding functionality.33 When running as a server (via codex mcp-server), the Codex CLI exposes tools like codex() for starting conversations and codex-reply() for continuing them.33  
This protocol allows for "intelligent routing" where a central router (GPT-5.4) receives a request via an API or voice command and then selects the appropriate MCP agent to handle the subtask.32 For example, a request to "Review security in the new PR" is routed to a GLM 5.1 worker that has a specialized security-review skill installed as an MCP server.32

### **Context Management and Persistence**

The Codex app-server is the interface used to power these deep integrations. It manages "Threads" (conversations), "Turns" (user requests), and "Items" (discrete units of work like file changes or tool calls).31 For long-running sessions, thread lifecycle and persistence are critical; the app-server creates, resumes, and archives threads, persisting the event history so that a client can reconnect at any time and see a consistent project timeline.36  
To manage context in "hours on end" sessions, the app-server supports "auto-compaction" of the context window.1 This ensures that as the session grows, the most relevant information is preserved while older, less critical conversation data is summarized or removed to stay within the model's limits.1

## **Environment Security: Sandboxing and Isolated Execution**

Allowing an autonomous swarm to manage a project for hours requires rigorous security to prevent unauthorized filesystem or network access. The Codex CLI utilizes OS-level sandboxing, enforcing policies at the kernel level rather than inside containers.1

### **Sandbox Modes and Human Approval**

The level of autonomy granted to the swarm is controlled via the \--sandbox and \--ask-for-approval flags.37

| Mode | Filesystem Access | Network Access | Recommended Use Case |
| :---- | :---- | :---- | :---- |
| **read-only** | Read only | Restricted | Initial scouting and planning.16 |
| **workspace-write** | Read/Write in current dir | Restricted | Implementation by GLM 5.1 workers.37 |
| **danger-full-access** | Unrestricted | Unrestricted | High-trust environments or CI/CD loops.37 |

For "direction and go" operations, the approval policy is typically set to never or on-request with the orchestrator handling the decision logic.37 In a high-security setup, the swarm can be run inside an "Incus Jail," a system container that allows public internet access for documentation while blocking access to the local network (LAN) and sensitive host directories.40

### **Implementation of the Incus Jail**

The Incus jail model provides a "head-less" workspace that behaves like a real workstation but is isolated from the user's primary environment.40 This is essential when running models like GLM 5.1, which may autonomously decide to install new packages or run complex build scripts.40 The jail ensures that any "runaway" build or unexpected network request is contained within the container's boundaries.40

## **Operational Strategy: Managing the Swarm for Hours**

The successful execution of a multi-hour engineering task depends on the orchestrator's ability to maintain a clear trajectory. This is achieved through the use of project-level operating contracts, such as AGENTS.md and CLAUDE.md, which define the "rules of engagement" for all agents in the swarm.1

### **Direction Injection and The Agent Loop**

The initial "direction" is injected into the swarm via a comprehensive system prompt that points the models at the environment, the strategy for producing artifacts, and the final completion conditions.42 The orchestrator then enters the "agent loop," a continuous cycle of:

1. **Perceive:** Using Qwen scouts to gather the current state of the repo.10  
2. **Plan:** Using GPT-5.4 to decompose the direction into next steps.2  
3. **Execute:** Using GLM 5.1 to implement code changes.3  
4. **Verify:** Using local test runners and Spark 5.3 for rapid review.5

### **Status Reporting and Heartbeats**

To keep the user informed during "direction and go" sessions, the orchestrator can be configured to produce "proactive heartbeats".27 Every defined interval, the agent gathers its own context—system health, git status, project file changes—and updates a HEARTBEAT.md file or sends a notification through a channel like Telegram or Discord.26 This allows the user to monitor the swarm's progress from their phone and intervene only if the agent signals a hard blockage.6

## **Comparative Analysis of Model Costs and Usage**

Running a multi-model swarm for hours can incur significant costs if not managed correctly. Using the OpenCode harness and local scouts is a critical strategy for maintaining financial viability.

| Model / Harness | Input Cost (per 1M) | Output Cost (per 1M) | Persistence Method |
| :---- | :---- | :---- | :---- |
| **GPT-5.4 (Codex)** | $0.95 | $3.15 | Cloud Threads 3 |
| **GLM 5.1 (OpenCode)** | $0.50 | $3.00 | Local JSONL 10 |
| **Spark 5.3 (Codex)** | Free (test-tier) | Free (test-tier) | Ephemeral 15 |
| **Qwen 3.5 9B (Local)** | $0.00 | $0.00 | Local Memory 22 |

3  
The use of local Qwen scouts for the hundreds of daily "read" operations and Spark 5.3 for rapid debugging ensures that the more expensive frontier models are only utilized for tasks that truly require their reasoning capacity. This tiered approach allows for "hours on end" operations that are both powerful and economically sustainable.

## **Conclusion: The Engineering Swarm as Infrastructure**

The combination of GPT-5.4, GLM 5.1, Spark 5.3, and Qwen 3.5 represents the state-of-the-art in autonomous engineering swarms. By utilizing the Codex CLI for orchestration, the OpenCode harness for high-horizon implementation, and robust scheduling via cron and systemd, developers can now deploy "direction and go" systems that manage complex projects with high reliability. The success of these systems is not just in the intelligence of the individual models, but in the connective protocols (MCP) and security architectures (Incus Jails) that allow them to function as a coherent, autonomous workforce. As context windows continue to expand and small models gain frontier-level coding abilities, the role of the human engineer will increasingly shift from writing code to managing the strategic direction and review of these autonomous agentic swarms.1

#### **Works cited**

1. Codex CLI: The Definitive Technical Reference \- Blake Crosley, accessed April 14, 2026, [https://blakecrosley.com/guides/codex](https://blakecrosley.com/guides/codex)  
2. Testing GPT-5.4: We collapsed a complex multi-agent workflow down to just two agents., accessed April 14, 2026, [https://www.reddit.com/r/AI\_Agents/comments/1rndgig/testing\_gpt54\_we\_collapsed\_a\_complex\_multiagent/](https://www.reddit.com/r/AI_Agents/comments/1rndgig/testing_gpt54_we_collapsed_a_complex_multiagent/)  
3. Models | OpenRouter, accessed April 14, 2026, [https://openrouter.ai/models](https://openrouter.ai/models)  
4. feat: /cron — In-session periodic message injection · Issue \#8707 · openai/codex \- GitHub, accessed April 14, 2026, [https://github.com/openai/codex/issues/8707](https://github.com/openai/codex/issues/8707)  
5. How to stop your autoresearch loop from cheating \- Cerebras, accessed April 14, 2026, [https://www.cerebras.ai/blog/how-to-stop-your-autoresearch-loop-from-cheating](https://www.cerebras.ai/blog/how-to-stop-your-autoresearch-loop-from-cheating)  
6. OpenClaw in Practice: Self-Hosted AI Agent Running in 10 Minutes, Wired to Discord, accessed April 14, 2026, [https://benterminal.com/en/posts/2026/openclaw-self-hosted-agent/](https://benterminal.com/en/posts/2026/openclaw-self-hosted-agent/)  
7. Multi-Agent Orchestration: How to Coordinate AI Agents at Scale \- GuruSup, accessed April 14, 2026, [https://gurusup.com/blog/multi-agent-orchestration-guide](https://gurusup.com/blog/multi-agent-orchestration-guide)  
8. Benchmarking LLMs with Marimo Pair \- Eric J. Ma's Personal Site, accessed April 14, 2026, [https://ericmjl.github.io/blog/2026/4/8/benchmarking-llms-with-marimo-pair/](https://ericmjl.github.io/blog/2026/4/8/benchmarking-llms-with-marimo-pair/)  
9. Blog | MindStudio, accessed April 14, 2026, [https://www.mindstudio.ai/blog](https://www.mindstudio.ai/blog)  
10. AI Models Marketplace \- Compare 500+ LLM APIs \- Infron AI, accessed April 14, 2026, [https://infron.ai/models](https://infron.ai/models)  
11. Swapped my claude code config to glm-5.1 after a youtuber I follow tested it \- Reddit, accessed April 14, 2026, [https://www.reddit.com/r/ClaudeCode/comments/1skazj6/swapped\_my\_claude\_code\_config\_to\_glm51\_after\_a/](https://www.reddit.com/r/ClaudeCode/comments/1skazj6/swapped_my_claude_code_config_to_glm51_after_a/)  
12. How I run long tasks with Claude Code and Codex talking to and reviewing each other, accessed April 14, 2026, [https://www.reddit.com/r/ClaudeCode/comments/1rht68z/how\_i\_run\_long\_tasks\_with\_claude\_code\_and\_codex/](https://www.reddit.com/r/ClaudeCode/comments/1rht68z/how_i_run_long_tasks_with_claude_code_and_codex/)  
13. How has your experience been using GLM as a replacement for codex and claude code professionally? : r/ZaiGLM \- Reddit, accessed April 14, 2026, [https://www.reddit.com/r/ZaiGLM/comments/1sfsy33/how\_has\_your\_experience\_been\_using\_glm\_as\_a/](https://www.reddit.com/r/ZaiGLM/comments/1sfsy33/how_has_your_experience_been_using_glm_as_a/)  
14. Gemma 4 vs Qwen 3.5: Which Open-Weight Model Should You Use for Local AI Workflows?, accessed April 14, 2026, [https://www.mindstudio.ai/blog/gemma-4-vs-qwen-3-5-open-weight-comparison](https://www.mindstudio.ai/blog/gemma-4-vs-qwen-3-5-open-weight-comparison)  
15. Just canceled. : r/ClaudeCode \- Reddit, accessed April 14, 2026, [https://www.reddit.com/r/ClaudeCode/comments/1siez15/just\_canceled/](https://www.reddit.com/r/ClaudeCode/comments/1siez15/just_canceled/)  
16. Codex CLI Cheat Sheet & Quick Reference \- CheatSheets.zip, accessed April 14, 2026, [https://cheatsheets.zip/codex-cli](https://cheatsheets.zip/codex-cli)  
17. Automations – Codex app \- OpenAI Developers, accessed April 14, 2026, [https://developers.openai.com/codex/app/automations](https://developers.openai.com/codex/app/automations)  
18. jonnyzzz/run-agent: A multi-agent orchestration framework for AI-powered software development \- GitHub, accessed April 14, 2026, [https://github.com/jonnyzzz/run-agent](https://github.com/jonnyzzz/run-agent)  
19. Around the Horn Digest: Everything That Happened in AI This Week (Mar 1-7, 2026\) \- The Neuron, accessed April 14, 2026, [https://www.theneuron.ai/ai-news-digests/around-the-horn-digest-everything-that-happened-in-ai-this-week-mar-1-7-2026/](https://www.theneuron.ai/ai-news-digests/around-the-horn-digest-everything-that-happened-in-ai-this-week-mar-1-7-2026/)  
20. Qwen 3.5 vs Llama vs Mistral: China's Open-Source AI Is Catching Up Faster Than You Think, accessed April 14, 2026, [https://www.aimagicx.com/blog/qwen-3-5-vs-llama-vs-mistral-china-open-source-ai-2026](https://www.aimagicx.com/blog/qwen-3-5-vs-llama-vs-mistral-china-open-source-ai-2026)  
21. AI Models \- Puter Developer, accessed April 14, 2026, [https://developer.puter.com/ai/models/](https://developer.puter.com/ai/models/)  
22. Qwen 3.5 9B Review: Alibaba's Open Source Model Tested \- Emelia.io, accessed April 14, 2026, [https://emelia.io/hub/qwen-35-9b-review](https://emelia.io/hub/qwen-35-9b-review)  
23. haowjy/orchestrate: skills to orchestrate multiple harnesses together \- GitHub, accessed April 14, 2026, [https://github.com/haowjy/orchestrate](https://github.com/haowjy/orchestrate)  
24. Which is the best local LLM in April 2026 for a 16 GB GPU? I'm looking for an ultimate model for some chat, light coding, and experiments with agent building. \- Reddit, accessed April 14, 2026, [https://www.reddit.com/r/LocalLLM/comments/1sj9c4c/which\_is\_the\_best\_local\_llm\_in\_april\_2026\_for\_a/](https://www.reddit.com/r/LocalLLM/comments/1sj9c4c/which_is_the_best_local_llm_in_april_2026_for_a/)  
25. Local LLM Hardware Requirements in 2026 | AI Hub, accessed April 14, 2026, [https://overchat.ai/ai-hub/llm-hardware-requirements](https://overchat.ai/ai-hub/llm-hardware-requirements)  
26. I built a CLI that runs Codex on a schedule and opens PRs while I ..., accessed April 14, 2026, [https://www.reddit.com/r/codex/comments/1rs3q1g/i\_built\_a\_cli\_that\_runs\_codex\_on\_a\_schedule\_and/](https://www.reddit.com/r/codex/comments/1rs3q1g/i_built_a_cli_that_runs_codex_on_a_schedule_and/)  
27. redbuilding/osoba: This app demonstrates use of MCP server and client in a local model chat via Ollama that incorporates web search via Serper. \- GitHub, accessed April 14, 2026, [https://github.com/redbuilding/ollama-chat-with-mcp](https://github.com/redbuilding/ollama-chat-with-mcp)  
28. Run Claude Code in the Cloud — No Install Needed \- Duet, accessed April 14, 2026, [https://duet.so/blog/how-to-run-claude-code-in-the-cloud](https://duet.so/blog/how-to-run-claude-code-in-the-cloud)  
29. How to resolve systemd service errors for OpenClaw on Linux \- Tencent Cloud, accessed April 14, 2026, [https://www.tencentcloud.com/techpedia/141216](https://www.tencentcloud.com/techpedia/141216)  
30. Building an AI Agent Mesh with Gemini 3, OpenClaw, and ACPX | by Timothy \- Medium, accessed April 14, 2026, [https://timtech4u.medium.com/building-an-ai-agent-mesh-with-gemini-3-openclaw-and-acpx-7b6ab5f1cbf4](https://timtech4u.medium.com/building-an-ai-agent-mesh-with-gemini-3-openclaw-and-acpx-7b6ab5f1cbf4)  
31. Codex App Server \- OpenAI Developers, accessed April 14, 2026, [https://developers.openai.com/codex/app-server](https://developers.openai.com/codex/app-server)  
32. Codex MCP Orchestra | MCP Servers \- LobeHub, accessed April 14, 2026, [https://lobehub.com/mcp/hvkshetry-codex-mcp-orchestra](https://lobehub.com/mcp/hvkshetry-codex-mcp-orchestra)  
33. Use Codex with the Agents SDK | OpenAI Developers, accessed April 14, 2026, [https://developers.openai.com/codex/guides/agents-sdk](https://developers.openai.com/codex/guides/agents-sdk)  
34. Building Consistent Workflows with Codex CLI & Agents SDK \- OpenAI Developers, accessed April 14, 2026, [https://developers.openai.com/cookbook/examples/codex/codex\_mcp\_agents\_sdk/building\_consistent\_workflows\_codex\_cli\_agents\_sdk](https://developers.openai.com/cookbook/examples/codex/codex_mcp_agents_sdk/building_consistent_workflows_codex_cli_agents_sdk)  
35. My Ultimate Prod Test \- Figma to Code 11 App Screens in 1 go \- GPT5 (High) vs Claude Code Opus 4.1, accessed April 14, 2026, [https://www.reddit.com/r/ClaudeCode/comments/1n9wgkw/my\_ultimate\_prod\_test\_figma\_to\_code\_11\_app/](https://www.reddit.com/r/ClaudeCode/comments/1n9wgkw/my_ultimate_prod_test_figma_to_code_11_app/)  
36. Unlocking the Codex harness: how we built the App Server | OpenAI, accessed April 14, 2026, [https://openai.com/index/unlocking-the-codex-harness/](https://openai.com/index/unlocking-the-codex-harness/)  
37. Agent approvals & security – Codex | OpenAI Developers, accessed April 14, 2026, [https://developers.openai.com/codex/agent-approvals-security](https://developers.openai.com/codex/agent-approvals-security)  
38. Command line options – Codex CLI \- OpenAI Developers, accessed April 14, 2026, [https://developers.openai.com/codex/cli/reference](https://developers.openai.com/codex/cli/reference)  
39. Codex CLI exec mode experiments: 81 flag/feature tests with raw ..., accessed April 14, 2026, [https://gist.github.com/alexfazio/359c17d84cb6a5af12bac88fa1db9770](https://gist.github.com/alexfazio/359c17d84cb6a5af12bac88fa1db9770)  
40. Incus System-Container Jail for the Codex Coding Agent \- Weisser Zwerg, accessed April 14, 2026, [https://weisser-zwerg.dev/posts/incus-codex-jail/](https://weisser-zwerg.dev/posts/incus-codex-jail/)  
41. Reduced Opus 4.6 consumption by integrating with GLM-5 while preserving its parallelism, accessed April 14, 2026, [https://www.reddit.com/r/ClaudeAI/comments/1r4s61z/reduced\_opus\_46\_consumption\_by\_integrating\_with/](https://www.reddit.com/r/ClaudeAI/comments/1r4s61z/reduced_opus_46_consumption_by_integrating_with/)  
42. Unrolling the Codex agent loop | OpenAI, accessed April 14, 2026, [https://openai.com/index/unrolling-the-codex-agent-loop/](https://openai.com/index/unrolling-the-codex-agent-loop/)  
43. BioAgent Bench: An AI Agent Evaluation Suite for Bioinformatics \- arXiv, accessed April 14, 2026, [https://arxiv.org/html/2601.21800v1](https://arxiv.org/html/2601.21800v1)  
44. Blogmarks \- Simon Willison's Weblog, accessed April 14, 2026, [https://simonwillison.net/blogmarks/](https://simonwillison.net/blogmarks/)