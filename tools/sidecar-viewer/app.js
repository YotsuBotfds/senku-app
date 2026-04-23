(() => {
  const contract = window.SidecarViewerContract;
  const bridgeBaseUrl = contract.bridge.baseUrl.replace(/\/$/, '');
  const streamBaseUrl = (contract.bridge.streamBaseUrl || bridgeBaseUrl).replace(/\/$/, '');
  const pollIntervalMs = contract.bridge.pollIntervalMs || 5000;
  const streamPollMs = contract.bridge.streamPollMs || pollIntervalMs;
  const listLimit = contract.bridge.listLimit || 200;
  const runningStates = new Set(contract.bridge.runningStates || ['busy', 'submitted', 'queued']);
  const staleThresholdMs = contract.bridge.staleThresholdMs || (15 * 60 * 1000);
  const dom = Object.fromEntries(Object.entries(contract.domIds).map(([key, id]) => [key, document.getElementById(id)]));
  const fallbackDomIds = {
    selectedTaskPromptPreview: 'selectedTaskPromptPreview',
    selectedTaskFollowupPreview: 'selectedTaskFollowupPreview',
  };
  Object.entries(fallbackDomIds).forEach(([key, id]) => {
    if (!dom[key]) dom[key] = document.getElementById(id);
  });

  dom.selectedTaskPromptBubble = document.getElementById('selectedTaskPromptBubble');
  dom.selectedTaskFollowupBubble = document.getElementById('selectedTaskFollowupBubble');
  dom.msgPrompt = document.getElementById('msgPrompt');
  dom.msgFollowup = document.getElementById('msgFollowup');
  dom.msgTranscript = document.getElementById('msgTranscript');
  dom.msgLatestResult = document.getElementById('msgLatestResult');
  dom.msgFinalResult = document.getElementById('msgFinalResult');
  dom.chatTranscript = document.getElementById('chatTranscript');
  dom.resultLatestBubble = document.getElementById('resultLatestBubble');
  dom.resultFinalBubble = document.getElementById('resultFinalBubble');
  dom.resultLatestTime = document.getElementById('resultLatestTime');
  dom.resultFinalTime = document.getElementById('resultFinalTime');
  dom.selectedTaskStateBadge = document.getElementById('selectedTaskStateBadge');
  dom.chatFlow = document.getElementById('chatFlow');
  dom.typingIndicator = document.getElementById('typingIndicator');

  const state = {
    health: null,
    tasks: [],
    selectedTaskId: null,
    selectedTask: null,
    selectedMessages: [],
    latestResult: null,
    finalResult: null,
    streamTranscript: [],
    streamSeq: 0,
    streamRenderedText: '',
    autoRefresh: true,
    filter: 'all',
    search: '',
    pollTimer: null,
    refreshInFlight: false,
    selectedStream: null,
  };

  const taskStateClassMap = {
    busy: 'state-busy',
    running: 'state-busy',
    submitted: 'state-busy',
    queued: 'state-busy',
    retry: 'state-busy',
    completed: 'state-completed',
    failed: 'state-failed',
    aborted: 'state-aborted',
    abort_requested: 'state-aborted',
    timed_out: 'state-failed',
    stale: 'state-stale',
  };

  const liveStates = new Set(['busy', 'submitted', 'queued', 'retry']);
  const terminalStates = new Set(['completed', 'aborted', 'failed', 'timed_out', 'stale']);

  function apiUrl(path) {
    return `${bridgeBaseUrl}${path}`;
  }

  async function fetchJson(path) {
    const response = await fetch(apiUrl(path), { cache: 'no-store' });
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || `Request failed: ${response.status}`);
    }
    return response.json();
  }

  function show(element, visible) {
    if (!element) return;
    element.classList.toggle('hidden', !visible);
  }

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function asText(value) {
    if (value === null || value === undefined) return '';
    return String(value).trim();
  }

  function messageRole(message) {
    return asText(message?.info?.role || message?.role).toLowerCase();
  }

  function messageText(message) {
    if (!message) return '';
    const parts = Array.isArray(message.parts) ? message.parts : [];
    const textParts = parts
      .map((part) => asText(part?.text))
      .filter(Boolean);
    if (textParts.length) return textParts.join('\n').trim();
    return asText(
      message?.text ||
      message?.content ||
      message?.info?.summary?.text ||
      ''
    );
  }

  function messageTimestamp(message) {
    const value = message?.created_at ||
      message?.updated_at ||
      message?.timestamp ||
      message?.time ||
      message?.info?.created_at ||
      message?.info?.updated_at ||
      message?.info?.timestamp ||
      message?.info?.time ||
      null;
    const date = value ? new Date(value) : null;
    return date && !Number.isNaN(date.getTime()) ? date : null;
  }

  function roleLabel(role) {
    const normalized = asText(role).toLowerCase();
    if (normalized === 'assistant') return 'Assistant';
    if (normalized === 'user') return 'User';
    if (normalized === 'system') return 'System';
    if (normalized === 'tool') return 'Tool';
    if (normalized === 'function') return 'Function';
    return normalized ? normalized[0].toUpperCase() + normalized.slice(1) : 'Message';
  }

  function parseStreamEvent(event) {
    const raw = asText(event?.data);
    if (!raw) return {};
    try {
      return JSON.parse(raw);
    } catch {
      return { text: raw };
    }
  }

  function streamPayloadState(payload) {
    if (!payload || typeof payload !== 'object') return '';
    return asText(
      payload.raw_status ||
      payload.state ||
      payload.task_state ||
      payload.status ||
      ''
    ).toLowerCase();
  }

  function streamPayloadText(payload) {
    if (!payload || typeof payload !== 'object') return '';
    return asText(
      payload.text_completed ||
      payload.text_latest ||
      payload.text_selected ||
      payload.text ||
      payload.result_text ||
      payload.assistant_text ||
      payload.latest_preview ||
      payload.preview ||
      payload.message_text ||
      payload.content ||
      ''
    );
  }

  function appendStreamTranscriptItem(payload, eventName) {
    if (eventName === 'status' || eventName === 'task_state') {
      return;
    }
    const text = streamPayloadText(payload);
    const stateText = streamPayloadState(payload);
    let note = '';
    let delta = text;

    if (text) {
      const previous = asText(state.streamRenderedText);
      if (previous && text.startsWith(previous)) {
        delta = text.slice(previous.length);
      }
      if (!delta && stateText) {
        delta = `state: ${stateText}`;
        note = eventName;
      }
      if (delta) {
        state.streamTranscript.push({
          seq: state.streamSeq++,
          sortMs: Date.now(),
          role: 'assistant',
          kind: eventName || 'stream',
          label: eventName === 'task_result_final' || eventName === 'result' || eventName === 'done'
            ? 'Assistant final'
            : 'Assistant streaming',
          text: delta,
          note: note || (eventName ? eventName.replace(/_/g, ' ') : ''),
          time: new Date().toLocaleString(),
        });
        state.streamRenderedText = text;
      }
    }
  }

  function extractPromptFromMessages(messages) {
    if (!Array.isArray(messages)) return '';
    for (const message of messages) {
      if (messageRole(message) === 'user') {
        const text = messageText(message);
        if (text) return text;
      }
    }
    return '';
  }

  function transcriptEntryClass(role) {
    const normalized = asText(role).toLowerCase();
    if (normalized === 'user') return 'message--user';
    return 'message--assistant';
  }

  function transcriptEntryLabel(message, fallbackRole) {
    const role = messageRole(message) || asText(fallbackRole).toLowerCase();
    return roleLabel(role);
  }

  function transcriptEntryTime(message, fallbackTime = null) {
    const date = messageTimestamp(message) || fallbackTime;
    return date ? date.toLocaleString() : '';
  }

  function transcriptEntryText(message) {
    const text = messageText(message);
    if (text) return text;
    return asText(message?.info?.summary?.text || message?.info?.summary || '');
  }

  function transcriptItemHtml(item) {
    const klass = transcriptEntryClass(item.role);
    const label = escapeHtml(item.label || roleLabel(item.role));
    const time = item.time ? `<div class="message__time">${escapeHtml(item.time)}</div>` : '';
    const note = item.note ? `<div class="chat-transcript__note">${escapeHtml(item.note)}</div>` : '';
    return `
      <div class="message ${klass} chat-transcript__entry ${item.kind ? `chat-transcript__entry--${escapeHtml(item.kind)}` : ''}">
        <div class="message__label">${label}</div>
        <div class="message__bubble chat-transcript__bubble">${escapeHtml(item.text || 'No text returned.')}</div>
        ${note}
        ${time}
      </div>
    `;
  }

  function buildTranscriptItems() {
    const items = [];
    const orderedMessages = Array.isArray(state.selectedMessages) ? [...state.selectedMessages] : [];
    orderedMessages.forEach((message, index) => {
      const role = messageRole(message);
      const text = transcriptEntryText(message);
      if (!text) return;
      items.push({
        seq: index,
        sortMs: messageTimestamp(message)?.getTime() ?? index,
        role: role || 'assistant',
        kind: role || 'message',
        label: transcriptEntryLabel(message, role),
        text,
        time: transcriptEntryTime(message),
      });
    });

    const latestText = asText(pickText(state.latestResult));
    const finalText = asText(pickText(state.finalResult));
    const existingTexts = new Set(items.map((item) => asText(item.text)));

    if (latestText && latestText !== 'No result yet.' && latestText !== 'No text returned.' && !existingTexts.has(latestText)) {
      items.push({
        seq: items.length + 1,
        sortMs: parseTime(state.latestResult?.updated_at || state.latestResult?.created_at)?.getTime() ?? Date.now(),
        role: 'assistant',
        kind: 'latest-result',
        label: 'Assistant latest',
        text: latestText,
        time: isoOrDash(state.latestResult?.updated_at || state.latestResult?.created_at) === '-' ? '' : isoOrDash(state.latestResult?.updated_at || state.latestResult?.created_at),
      });
      existingTexts.add(latestText);
    }

    if (finalText && finalText !== 'No result yet.' && finalText !== 'No text returned.' && !existingTexts.has(finalText)) {
      items.push({
        seq: items.length + 1,
        sortMs: parseTime(state.finalResult?.updated_at || state.finalResult?.created_at)?.getTime() ?? (Date.now() + 1),
        role: 'assistant',
        kind: 'final-result',
        label: 'Assistant final',
        text: finalText,
        time: isoOrDash(state.finalResult?.updated_at || state.finalResult?.created_at) === '-' ? '' : isoOrDash(state.finalResult?.updated_at || state.finalResult?.created_at),
      });
    }

    const streamItems = Array.isArray(state.streamTranscript) ? state.streamTranscript : [];
    streamItems.forEach((item) => {
      items.push(item);
    });

    return items
      .filter((item) => asText(item.text))
      .sort((a, b) => {
        const aSort = Number.isFinite(a.sortMs) ? a.sortMs : (a.seq ?? 0);
        const bSort = Number.isFinite(b.sortMs) ? b.sortMs : (b.seq ?? 0);
        if (aSort !== bSort) return aSort - bSort;
        return (a.seq ?? 0) - (b.seq ?? 0);
      });
  }

  function renderTranscript() {
    if (!dom.chatTranscript || !dom.msgTranscript) return;
    const items = buildTranscriptItems();
    if (!items.length) {
      dom.chatTranscript.innerHTML = '';
      dom.msgTranscript.style.display = 'none';
      return;
    }
    dom.chatTranscript.innerHTML = items.map(transcriptItemHtml).join('');
    dom.msgTranscript.style.display = '';
  }

  function rawStatus(task) {
    return asText(task?.raw_status).toLowerCase();
  }

  function displayTaskState(task) {
    const raw = rawStatus(task);
    const taskState = asText(task?.state).toLowerCase();
    if (state.selectedStream && state.selectedStream.taskId === task?.task_id && !state.selectedStream.terminal) {
      return 'running';
    }
    if (liveStates.has(raw) || liveStates.has(taskState) || taskState === 'busy') return 'running';
    if (taskState) return taskState;
    if (raw === 'retry') return 'running';
    return raw || 'unknown';
  }

  function stateClassForTask(task) {
    return taskStateClassMap[displayTaskState(task)] || 'state-default';
  }

  function setTextContent(node, value, fallback = 'No text returned.') {
    if (!node) return;
    node.textContent = asText(value) || fallback;
  }

  function clip(text) {
    if (text === null || text === undefined || text === '') return 'No text returned.';
    return String(text);
  }

  function pretty(value) {
    try {
      return JSON.stringify(value ?? null, null, 2);
    } catch {
      return String(value);
    }
  }

  function pickText(result) {
    if (!result) return 'No result yet.';
    return clip(result.text_selected || result.text_completed || result.text_latest || result.text || 'No text returned.');
  }

  function pickFirstValue(task, keys) {
    for (const key of keys) {
      const value = asText(task?.[key]);
      if (value) return value;
    }
    return '';
  }

  function truncate(value, maxChars) {
    const text = asText(value);
    if (!text) return '';
    if (!maxChars || text.length <= maxChars) return text;
    return `${text.slice(0, Math.max(0, maxChars - 1))}...`;
  }

  function pickPromptPreview(task) {
    const fullPromptFromMessages = extractPromptFromMessages(state.selectedMessages);
    if (fullPromptFromMessages) return fullPromptFromMessages;
    const promptFields = pickFirstValue(task, [
      'prompt',
      'prompt_text',
      'input',
      'message',
    ]);
    if (promptFields) return promptFields;
    return pickFirstValue(task, [
      'prompt_preview',
      'title_prompt',
      'title',
      'label',
      'task_id',
    ]);
  }

  function pickSystemPromptPreview(task) {
    return pickFirstValue(task, [
      'system_prompt_preview',
      'systemText',
      'system_prompt',
    ]);
  }

  function pickFollowupPreview(task) {
    return pickFirstValue(task, [
      'last_followup_preview',
      'followup_preview',
      'followup',
    ]);
  }

  function pickProvider(task) {
    return pickFirstValue(task, [
      'provider_id',
      'providerID',
      'provider',
    ]);
  }

  function pickModel(task) {
    return pickFirstValue(task, [
      'model_id',
      'modelID',
      'model',
    ]);
  }

  function taskLabel(task) {
    return task.title || task.label || task.task_id || 'Untitled task';
  }

  function taskMetaLine(task) {
    const bits = [displayTaskState(task) || 'unknown'];
    if (task.label) bits.push(task.label);
    const model = pickModel(task);
    if (model) bits.push(model);
    return bits.join(' • ');
  }

  function isoOrDash(value) {
    if (!value) return '-';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  }

  function parseTime(value) {
    if (!value) return null;
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  function formatAge(ageMs) {
    if (ageMs === null || ageMs === undefined || !Number.isFinite(ageMs) || ageMs < 0) return 'age unknown';
    const totalSeconds = Math.floor(ageMs / 1000);
    if (totalSeconds < 60) return `${totalSeconds}s old`;
    const totalMinutes = Math.floor(totalSeconds / 60);
    if (totalMinutes < 60) return `${totalMinutes}m old`;
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    if (hours < 24) return minutes > 0 ? `${hours}h ${minutes}m old` : `${hours}h old`;
    const days = Math.floor(hours / 24);
    const remainingHours = hours % 24;
    return remainingHours > 0 ? `${days}d ${remainingHours}h old` : `${days}d old`;
  }

  function taskAgeMs(task) {
    if (typeof task?.age_ms === 'number' && Number.isFinite(task.age_ms)) {
      return task.age_ms;
    }
    const anchor = parseTime(
      task?.updated_at ||
      task?.status_updated_at ||
      task?.last_polled_at ||
      task?.last_started_at ||
      task?.created_at
    );
    if (!anchor) return null;
    return Math.max(0, Date.now() - anchor.getTime());
  }

  function taskStaleThresholdMs(task) {
    if (typeof task?.stale_after_ms === 'number' && Number.isFinite(task.stale_after_ms) && task.stale_after_ms > 0) {
      return task.stale_after_ms;
    }
    if (typeof task?.stale_after_seconds === 'number' && Number.isFinite(task.stale_after_seconds) && task.stale_after_seconds > 0) {
      return task.stale_after_seconds * 1000;
    }
    if (typeof task?.timeout_seconds === 'number' && Number.isFinite(task.timeout_seconds) && task.timeout_seconds > 0) {
      return task.timeout_seconds * 1000;
    }
    return staleThresholdMs;
  }

  function isTaskActiveLike(task) {
    if (typeof task?.is_active_like === 'boolean') return task.is_active_like;
    const taskState = displayTaskState(task);
    return runningStates.has(taskState) || taskState === 'running';
  }

  function isTaskStale(task) {
    if (typeof task?.is_stale_now === 'boolean') return task.is_stale_now;
    if (!isTaskActiveLike(task)) return false;
    const ageMs = taskAgeMs(task);
    if (ageMs === null) return false;
    return ageMs >= taskStaleThresholdMs(task);
  }

  function setLastRefresh(ok, message) {
    dom.lastRefresh.textContent = `Last refresh: ${new Date().toLocaleTimeString()}`;
    dom.lastRefreshError.textContent = ok ? '' : message;
  }

  function setTextFromPayload(node, payload, fallback, keyOrder = []) {
    const value = pickFirstValue(payload, keyOrder);
    setTextContent(node, value, fallback);
  }

  function filteredTasks() {
    const search = state.search.trim().toLowerCase();
    const searchHit = (task) => {
      if (!search) return true;
      const haystack = [
        task.task_id,
        task.label,
        task.title,
        task.state,
        task.prompt_preview,
        task.system_prompt_preview,
        task.last_followup_preview,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(search);
    };

    return state.tasks
      .filter((task) => {
      if (state.filter === 'all') return true;
      if (state.filter === 'running') return isTaskActiveLike(task);
      if (state.filter === 'busy') {
        return rawStatus(task) === 'busy' || displayTaskState(task) === 'running';
      }
      return (task.state || '').toLowerCase() === state.filter ||
        rawStatus(task) === state.filter ||
        displayTaskState(task) === state.filter;
    })
      .filter(searchHit)
      .slice(0, listLimit);
  }

  function runningTasks() {
    return state.tasks.filter((task) => isTaskActiveLike(task));
  }

  function pickAutoSelectedTaskId() {
    const visible = filteredTasks();
    const preferredVisible = visible.find((task) => isTaskActiveLike(task)) || visible[0] || null;
    if (preferredVisible?.task_id) return preferredVisible.task_id;

    const active = runningTasks();
    if (active[0]?.task_id) return active[0].task_id;

    return state.tasks[0]?.task_id || null;
  }

  function selectedTaskIsLiveCandidate(task) {
    if (!task || !task.task_id) return false;
    const raw = rawStatus(task);
    const taskState = asText(task.state).toLowerCase();
    if (liveStates.has(raw) || liveStates.has(taskState) || raw === 'retry' || taskState === 'retry') return true;
    if (terminalStates.has(raw) || terminalStates.has(taskState)) return false;
    if (!task.last_completed_at) return true;
    if (!state.finalResult && !state.latestResult) return true;
    return false;
  }

  function closeSelectedStream() {
    if (!state.selectedStream) return;
    try {
      state.selectedStream.eventSource?.close?.();
    } catch {
      // ignore close failures
    }
    state.selectedStream = null;
  }

  function syncSelectedStream() {
    const task = state.selectedTask;
    if (!task || !task.task_id) {
      closeSelectedStream();
      return;
    }

    const shouldStream = selectedTaskIsLiveCandidate(task);
    if (!shouldStream) {
      closeSelectedStream();
      return;
    }

    if (state.selectedStream?.taskId === task.task_id && !state.selectedStream.terminal) {
      return;
    }

    closeSelectedStream();

    const taskId = task.task_id;
    const url = `${streamBaseUrl}/sidecar/tasks/${encodeURIComponent(taskId)}/events?pollMs=${encodeURIComponent(streamPollMs)}`;
    const eventSource = new EventSource(url);
    state.selectedStream = {
      taskId,
      eventSource,
      connected: false,
      terminal: false,
      lastEventAt: null,
      lastEventName: null,
    };

    const updateTaskFromPayload = (payload, eventName) => {
      if (!payload || typeof payload !== 'object') return;
      const current = state.selectedTask && state.selectedTask.task_id === taskId
        ? { ...state.selectedTask }
        : { task_id: taskId };
      const nestedTask = payload.task && typeof payload.task === 'object' ? payload.task : null;
      const merged = {
        ...current,
        ...(nestedTask || {}),
        ...payload,
      };
      const normalizedState = eventName === 'task_state' || eventName === 'status'
        ? streamPayloadState(payload)
        : streamPayloadState(merged);
      if (normalizedState) {
        merged.raw_status = normalizedState;
        merged.state = normalizedState;
      } else if (merged.state && !merged.raw_status) {
        merged.raw_status = merged.state;
      } else if (merged.raw_status && !merged.state) {
        merged.state = merged.raw_status;
      }
      state.selectedTask = merged;
    };

    const updateResultsFromPayload = (payload, eventName) => {
      const text = streamPayloadText(payload);
      appendStreamTranscriptItem(payload, eventName);
      if (!text) return;
      const stamp = payload.created_at || payload.updated_at || new Date().toISOString();
      const baseResult = {
        text_latest: text,
        text_selected: text,
        text: text,
        created_at: stamp,
        updated_at: payload.updated_at || stamp,
      };
      if (eventName === 'task_result_final' || eventName === 'result' || eventName === 'done' || payload.has_completed_message) {
        state.latestResult = { ...state.latestResult, ...baseResult };
        state.finalResult = { ...state.finalResult, ...baseResult };
        state.selectedStream.terminal = true;
      } else {
        state.latestResult = { ...state.latestResult, ...baseResult };
      }
    };

    const finalizeStream = (payload, eventName) => {
      if (payload && typeof payload === 'object') {
        updateTaskFromPayload(payload, eventName);
        updateResultsFromPayload(payload, eventName);
      }
      if (!state.selectedStream || state.selectedStream.taskId !== taskId) return;
      state.selectedStream.lastEventAt = Date.now();
      state.selectedStream.lastEventName = eventName;
      const currentState = streamPayloadState(payload) || rawStatus(state.selectedTask) || asText(state.selectedTask?.state).toLowerCase();
      if (terminalStates.has(currentState) || eventName === 'done' || eventName === 'task_result_final') {
        state.selectedStream.terminal = true;
      }
      renderTaskList();
      renderDetail();
      if (state.selectedStream?.terminal) {
        closeSelectedStream();
      }
    };

    eventSource.addEventListener('open', () => {
      if (!state.selectedStream || state.selectedStream.taskId !== taskId) return;
      state.selectedStream.connected = true;
      state.selectedStream.lastEventName = 'open';
      renderDetail();
    });
    eventSource.addEventListener('task_state', (event) => finalizeStream(parseStreamEvent(event), 'task_state'));
    eventSource.addEventListener('status', (event) => finalizeStream(parseStreamEvent(event), 'status'));
    eventSource.addEventListener('task_result_partial', (event) => finalizeStream(parseStreamEvent(event), 'task_result_partial'));
    eventSource.addEventListener('task_result_final', (event) => finalizeStream(parseStreamEvent(event), 'task_result_final'));
    eventSource.addEventListener('result', (event) => finalizeStream(parseStreamEvent(event), 'result'));
    eventSource.addEventListener('done', (event) => finalizeStream(parseStreamEvent(event), 'done'));
    eventSource.addEventListener('task_error', (event) => finalizeStream(parseStreamEvent(event), 'task_error'));
    eventSource.addEventListener('message', (event) => finalizeStream(parseStreamEvent(event), 'message'));
    eventSource.addEventListener('heartbeat', () => {
      if (!state.selectedStream || state.selectedStream.taskId !== taskId) return;
      state.selectedStream.lastEventAt = Date.now();
    });
    eventSource.onerror = () => {
      if (!state.selectedStream || state.selectedStream.taskId !== taskId) return;
      state.selectedStream.connected = false;
      renderDetail();
    };
  }

  function renderHealth() {
    const health = state.health;
    const bridgeOk = Boolean(health && (health.ok || health.healthy));
    const upstreamOk = Boolean(health?.opencode?.healthy);
    const ok = bridgeOk && upstreamOk;
    const degraded = bridgeOk && !upstreamOk;
    dom.healthBadge.textContent = ok
      ? 'Bridge healthy'
      : (degraded ? 'Bridge degraded' : 'Bridge unavailable');
    dom.healthBadge.className = `pill pill--health ${ok ? 'pill--ok' : 'pill--alert'}`;
    dom.healthText.textContent = ok
      ? `Connected to ${bridgeBaseUrl}`
      : (degraded
        ? `Bridge up, upstream OpenCode down at ${health?.opencode?.base_url || 'unknown'}`
        : 'Viewer cannot reach the local sidecar bridge right now.');
    dom.bridgeUrl.textContent = `Bridge: ${bridgeBaseUrl}`;
  }

  function renderTaskList() {
    const tasks = filteredTasks();
    const running = runningTasks();

    dom.taskCount.textContent = `${tasks.length} shown`;
    dom.runningCount.textContent = `${running.length} running`;
    dom.runningCount.disabled = running.length === 0;
    dom.runningCount.title = running.length === 1
      ? 'Click to jump to the only running task.'
      : 'Click to filter to all running tasks.';

    dom.taskList.innerHTML = '';
    show(dom.taskListEmptyState, tasks.length === 0);

    tasks.forEach((task) => {
      const sourceTask = (state.selectedTaskId && task.task_id === state.selectedTaskId && state.selectedTask)
        ? state.selectedTask
        : task;
      const stale = isTaskStale(sourceTask);
      const ageText = formatAge(taskAgeMs(sourceTask));
      const helperBits = [taskMetaLine(sourceTask), ageText];
      const promptPreview = truncate(pickPromptPreview(sourceTask), 150);
      const displayState = displayTaskState(sourceTask);
      const resultReady = displayState === 'completed';
      const button = document.createElement('button');
      button.type = 'button';
      button.className = `task-row ${state.selectedTaskId === task.task_id ? 'task-row--selected' : ''}`;
      button.dataset.taskId = task.task_id;
      button.innerHTML = `
        <div class="task-row__top">
          <span class="task-row__title">${escapeHtml(taskLabel(task))}</span>
          <span class="task-row__badges">
            ${stale ? `<span class="task-row__age task-row__age--stale">${escapeHtml(ageText)}</span>` : `<span class="task-row__age">${escapeHtml(ageText)}</span>`}
            ${resultReady ? `<span class="task-row__ready">ready</span>` : ``}
            <span class="task-row__state ${stateClassForTask(task)}">${escapeHtml(displayState)}</span>
          </span>
        </div>
        <div class="task-row__meta">${escapeHtml(helperBits.join(' • '))}</div>
        <div class="task-row__submeta">${escapeHtml(task.task_id || '')}</div>
        <div class="task-row__submeta">${escapeHtml(promptPreview || 'No prompt preview')}</div>
      `;
      button.addEventListener('click', () => selectTask(task.task_id));
      dom.taskList.appendChild(button);
    });
  }

  function renderDetail() {
    const task = state.selectedTask;
    const hasSelection = Boolean(task);
    show(dom.detailEmptyState, !hasSelection);
    show(dom.detailContent, hasSelection);
    if (!hasSelection) {
      dom.selectedTaskHeader.textContent = 'Pick a task';
      dom.selectedTaskMeta.textContent = 'Choose a task from the left to inspect its current state and results.';
      if (dom.msgPrompt) dom.msgPrompt.style.display = 'none';
      dom.msgFollowup.style.display = 'none';
      if (dom.msgTranscript) dom.msgTranscript.style.display = 'none';
      dom.msgLatestResult.style.display = 'none';
      dom.msgFinalResult.style.display = 'none';
      if (dom.typingIndicator) dom.typingIndicator.style.display = 'none';
      if (dom.selectedTaskStateBadge) {
        dom.selectedTaskStateBadge.className = 'chat-status';
        dom.selectedTaskStateBadge.textContent = '-';
      }
      return;
    }

    const stale = isTaskStale(task);
    const ageText = formatAge(taskAgeMs(task));
    const displayState = displayTaskState(task);
    dom.selectedTaskHeader.textContent = taskLabel(task);
    dom.selectedTaskMeta.textContent = stale
      ? `${task.task_id || 'No task id'} • flagged stale • ${ageText}`
      : `${task.task_id || 'No task id'} • ${displayState === 'running' ? 'live turn' : 'settled'} • ${ageText}`;

    const stateClass = stateClassForTask(task);
    const isActive = isTaskActiveLike(task);
    if (dom.msgPrompt) dom.msgPrompt.style.display = 'none';
    dom.msgFollowup.style.display = 'none';
    dom.msgLatestResult.style.display = 'none';
    dom.msgFinalResult.style.display = 'none';
    if (dom.selectedTaskStateBadge) {
      const streamActive = Boolean(state.selectedStream && state.selectedStream.taskId === task.task_id && !state.selectedStream.terminal);
      const streamConnected = Boolean(streamActive && state.selectedStream.connected);
      dom.selectedTaskStateBadge.className = `chat-status ${stateClass} ${(isActive || streamActive) ? 'chat-status--running' : ''} ${streamConnected ? 'chat-status--streaming' : ''}`;
      dom.selectedTaskStateBadge.textContent = streamActive
        ? (streamConnected ? 'running • streaming' : 'running • connecting')
        : (displayState || '-');
    }

    dom.selectedTaskState.textContent = displayState || '-';
    dom.selectedTaskUpdated.textContent = isoOrDash(task.updated_at || task.status_updated_at || task.last_polled_at);
    dom.selectedTaskCreated.textContent = isoOrDash(task.created_at);
    dom.selectedTaskSubmissions.textContent = task.submission_count ?? '-';
    dom.selectedTaskProvider.textContent = pickProvider(task) || '-';
    dom.selectedTaskModel.textContent = pickModel(task) || '-';
    if (task.abort_requested_at) {
      dom.selectedTaskAbortRequested.textContent = `yes — ${isoOrDash(task.abort_requested_at)}`;
    } else {
      dom.selectedTaskAbortRequested.textContent = task.abort_requested ? 'yes' : 'no';
    }
    dom.selectedTaskLastError.textContent = task.last_error || '-';

    if (dom.typingIndicator) {
      const streamActive = Boolean(state.selectedStream && state.selectedStream.taskId === task.task_id && !state.selectedStream.terminal);
      dom.typingIndicator.style.display = (isActive || streamActive) ? '' : 'none';
    }

    renderTranscript();

    dom.taskJson.textContent = pretty(task);
    dom.resultJson.textContent = pretty(state.latestResult);
    dom.finalResultJson.textContent = pretty(state.finalResult);
    dom.taskPaths.textContent = pretty({
      task_path: task.task_path,
      result_path: task.result_path,
      latest_result_path: task.latest_result_path,
      final_result_path: task.final_result_path,
    });
  }

  async function loadHealth() {
    try {
      state.health = await fetchJson('/health');
      dom.viewerError.textContent = '';
      renderHealth();
    } catch (error) {
      state.health = { ok: false, error: error.message };
      dom.viewerError.textContent = error.message;
      renderHealth();
    }
  }

  async function loadTasks() {
    const payload = await fetchJson('/sidecar/tasks');
    const tasks = Array.isArray(payload.tasks) ? payload.tasks : [];
    state.tasks = tasks;
    const stillExists = state.selectedTaskId && tasks.some((task) => task.task_id === state.selectedTaskId);
    if (!stillExists) {
      state.selectedTaskId = pickAutoSelectedTaskId();
      state.selectedTask = null;
      state.selectedMessages = [];
      state.latestResult = null;
      state.finalResult = null;
      state.streamTranscript = [];
      state.streamSeq = 0;
      state.streamRenderedText = '';
      closeSelectedStream();
    } else if (!state.selectedTaskId) {
      state.selectedTaskId = pickAutoSelectedTaskId();
    }
  }

  async function loadSelectedTask() {
    if (!state.selectedTaskId) {
      state.selectedTask = null;
      state.selectedMessages = [];
      state.latestResult = null;
      state.finalResult = null;
      state.streamTranscript = [];
      state.streamSeq = 0;
      state.streamRenderedText = '';
      closeSelectedStream();
      renderDetail();
      return;
    }

    state.selectedMessages = [];
    state.latestResult = null;
    state.finalResult = null;
    state.streamTranscript = [];
    state.streamSeq = 0;
    state.streamRenderedText = '';

    const results = await Promise.allSettled([
      fetchJson(`/sidecar/tasks/${encodeURIComponent(state.selectedTaskId)}`),
      fetchJson(`/sidecar/tasks/${encodeURIComponent(state.selectedTaskId)}/messages`),
      fetchJson(`/sidecar/tasks/${encodeURIComponent(state.selectedTaskId)}/result/latest`),
      fetchJson(`/sidecar/tasks/${encodeURIComponent(state.selectedTaskId)}/result/final`),
    ]);

    const [taskResult, messagesResult, latestResultResult, finalResultResult] = results;

    if (taskResult.status === 'fulfilled') {
      const raw = taskResult.value;
      state.selectedTask = raw.task || raw;
    } else {
      state.selectedTask = state.tasks.find((task) => task.task_id === state.selectedTaskId) || state.selectedTask;
      dom.detailErrorState.textContent = taskResult.reason?.message || 'Failed to load task.';
      show(dom.detailErrorState, true);
    }

    if (messagesResult.status === 'fulfilled') {
      const raw = messagesResult.value;
      state.selectedMessages = Array.isArray(raw.messages) ? raw.messages : [];
    } else {
      state.selectedMessages = [];
    }

    if (latestResultResult.status === 'fulfilled') {
      const raw = latestResultResult.value;
      state.latestResult = raw.result || raw;
    } else {
      state.latestResult = null;
    }

    if (finalResultResult.status === 'fulfilled') {
      const raw = finalResultResult.value;
      state.finalResult = raw.result || raw;
    } else {
      state.finalResult = null;
    }
    renderTaskList();
    renderDetail();
    syncSelectedStream();
  }

  async function refreshAll() {
    if (state.refreshInFlight) return;
    state.refreshInFlight = true;
    show(dom.taskListError, false);
    show(dom.detailErrorState, false);
    try {
      await loadHealth();
    } catch (error) {
      dom.viewerError.textContent = error.message;
    }
    try {
      await loadTasks();
      renderTaskList();
      await loadSelectedTask();
    } catch (error) {
      dom.taskListError.textContent = error.message;
      show(dom.taskListError, true);
      dom.detailErrorState.textContent = error.message;
      show(dom.detailErrorState, true);
      setLastRefresh(false, error.message);
      state.refreshInFlight = false;
      return;
    }
    setLastRefresh(true, '');
    state.refreshInFlight = false;
  }

  async function selectTask(taskId) {
    closeSelectedStream();
    state.selectedTaskId = taskId;
    state.selectedTask = state.tasks.find((task) => task.task_id === taskId) || state.selectedTask;
    if (!state.selectedTask && taskId) {
      state.selectedTask = { task_id: taskId, title: taskId, state: 'loading' };
    }
    renderTaskList();
    renderDetail();
    await loadSelectedTask();
  }

  async function onRunningClick() {
    const running = runningTasks();
    state.filter = 'running';
    dom.stateFilter.value = 'running';
    renderTaskList();
    if (running.length === 1) {
      await selectTask(running[0].task_id);
      return;
    }
  }

  function startPolling() {
    if (state.pollTimer) clearInterval(state.pollTimer);
    state.pollTimer = setInterval(() => {
      if (state.autoRefresh) {
        refreshAll();
      }
    }, pollIntervalMs);
  }

  function wireCollapsibleToggles() {
    document.querySelectorAll('.chat-collapsible__toggle').forEach((btn) => {
      btn.addEventListener('click', () => {
        const parent = btn.closest('.chat-collapsible');
        if (parent) parent.classList.toggle('chat-collapsible--open');
      });
    });
  }

  function wireEvents() {
    dom.refreshToggle.addEventListener('change', () => {
      state.autoRefresh = dom.refreshToggle.checked;
    });

    dom.stateFilter.addEventListener('change', () => {
      state.filter = dom.stateFilter.value;
      renderTaskList();
    });

    dom.searchBox.addEventListener('input', () => {
      state.search = dom.searchBox.value;
      renderTaskList();
    });

    dom.refreshButton.addEventListener('click', refreshAll);
    dom.runningCount.addEventListener('click', onRunningClick);
  }

  wireCollapsibleToggles();
  wireEvents();
  refreshAll();
  startPolling();
})();
