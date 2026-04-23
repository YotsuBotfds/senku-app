package com.senku.host;

import com.google.ai.edge.litertlm.Backend;
import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.EngineConfig;
import com.google.ai.edge.litertlm.Message;
import com.google.ai.edge.litertlm.MessageCallback;
import com.google.ai.edge.litertlm.SamplerConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class LiteRtOpenAiServer {
    private static final Gson GSON = new Gson();
    private static final String MODEL_PATH_ENV_VAR = "SENKU_LITERT_MODEL_PATH";
    private static final String DEFAULT_MODEL_ID = "gemma-4-e2b-it-litert";
    private static final List<String> KNOWN_MODEL_FILE_NAMES = List.of(
        "gemma-4-E4B-it.litertlm",
        "gemma-4-E4B-it.task",
        "gemma-4-E2B-it.litertlm",
        "gemma-4-E2B-it.task"
    );
    private static final SamplerConfig GREEDY_SAMPLER = new SamplerConfig(1, 1.0, 0.0, 0);

    private LiteRtOpenAiServer() {
    }

    public static void main(String[] args) throws Exception {
        Config config = Config.fromArgs(args);
        Files.createDirectories(config.cacheDir);
        Runner runner = new Runner(config);
        runner.initialize();

        HttpServer server = HttpServer.create(new InetSocketAddress(config.host, config.port), 0);
        server.createContext("/healthz", new HealthHandler(config, runner));
        server.createContext("/v1/models", new ModelsHandler(config));
        server.createContext("/v1/chat/completions", new CompletionsHandler(config, runner));
        server.setExecutor(Executors.newFixedThreadPool(4));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(0);
            runner.close();
        }));

        System.out.println("LiteRT host server listening on http://" + config.host + ":" + config.port + "/v1/chat/completions");
        System.out.println("Model path: " + config.modelPath);
        System.out.println("Model id: " + config.modelId);
        System.out.println("Backend: " + runner.getBackendLabel());
        System.out.println("Sampler: greedy(topK=1); client temperature ignored");
        server.start();
    }

    private static final class Config {
        final String host;
        final int port;
        final String modelId;
        final Path modelPath;
        final Path cacheDir;
        final List<String> backendOrder;

        private Config(String host, int port, String modelId, Path modelPath, Path cacheDir, List<String> backendOrder) {
            this.host = host;
            this.port = port;
            this.modelId = modelId;
            this.modelPath = modelPath;
            this.cacheDir = cacheDir;
            this.backendOrder = backendOrder;
        }

        static Config fromArgs(String[] args) {
            String host = "127.0.0.1";
            int port = 1235;
            String modelId = DEFAULT_MODEL_ID;
            Path modelPath = null;
            Path cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "senku-litert-cache");
            List<String> backendOrder = List.of("gpu", "cpu");

            Map<String, String> argValues = parseArgs(args);
            if (argValues.containsKey("--host")) {
                host = argValues.get("--host");
            }
            if (argValues.containsKey("--port")) {
                port = Integer.parseInt(argValues.get("--port"));
            }
            if (argValues.containsKey("--model-id")) {
                modelId = argValues.get("--model-id");
            }
            if (argValues.containsKey("--model-path")) {
                modelPath = Path.of(argValues.get("--model-path"));
            } else {
                String envModelPath = System.getenv(MODEL_PATH_ENV_VAR);
                if (envModelPath != null && !envModelPath.isBlank()) {
                    modelPath = Path.of(envModelPath.trim());
                } else {
                    modelPath = resolveDefaultModelPath();
                }
            }
            if (argValues.containsKey("--cache-dir")) {
                cacheDir = Path.of(argValues.get("--cache-dir"));
            }
            if (argValues.containsKey("--backend-order")) {
                backendOrder = parseBackendOrder(argValues.get("--backend-order"));
            }

            if (modelPath == null) {
                throw new IllegalArgumentException(
                    "Model path is required. Pass --model-path or set " + MODEL_PATH_ENV_VAR + "."
                );
            }
            Path resolvedModelPath = modelPath.toAbsolutePath().normalize();
            if (!Files.isRegularFile(resolvedModelPath)) {
                throw new IllegalArgumentException("Model file not found: " + resolvedModelPath);
            }
            if (modelId.isBlank() || DEFAULT_MODEL_ID.equals(modelId)) {
                modelId = inferModelIdFromPath(resolvedModelPath);
            }

            return new Config(host, port, modelId, resolvedModelPath, cacheDir.toAbsolutePath().normalize(), backendOrder);
        }

        private static Path resolveDefaultModelPath() {
            Path currentDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
            Path parentDir = currentDir.getParent();
            Path userHome = Path.of(System.getProperty("user.home", ".")).toAbsolutePath().normalize();

            ArrayList<Path> searchRoots = new ArrayList<>();
            searchRoots.add(currentDir);
            searchRoots.add(currentDir.resolve("models"));
            if (parentDir != null) {
                searchRoots.add(parentDir);
                searchRoots.add(parentDir.resolve("models"));
            }
            searchRoots.add(userHome.resolve("Downloads"));

            for (Path root : searchRoots) {
                for (String fileName : KNOWN_MODEL_FILE_NAMES) {
                    Path candidate = root.resolve(fileName).normalize();
                    if (Files.isRegularFile(candidate)) {
                        return candidate;
                    }
                }
            }
            return null;
        }

        private static String inferModelIdFromPath(Path modelPath) {
            String normalized = modelPath.getFileName().toString().toLowerCase(Locale.US);
            if (normalized.contains("e4b")) {
                return "gemma-4-e4b-it-litert";
            }
            if (normalized.contains("e2b")) {
                return "gemma-4-e2b-it-litert";
            }
            return DEFAULT_MODEL_ID;
        }

        private static List<String> parseBackendOrder(String raw) {
            ArrayList<String> order = new ArrayList<>();
            for (String item : raw.split(",")) {
                String normalized = item == null ? "" : item.trim().toLowerCase(Locale.US);
                if (normalized.isEmpty()) {
                    continue;
                }
                order.add(normalized);
            }
            if (order.isEmpty()) {
                order.add("gpu");
                order.add("cpu");
            }
            return Collections.unmodifiableList(order);
        }

        private static Map<String, String> parseArgs(String[] args) {
            java.util.LinkedHashMap<String, String> parsed = new java.util.LinkedHashMap<>();
            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                if (!arg.startsWith("--")) {
                    continue;
                }
                int equalsIndex = arg.indexOf('=');
                if (equalsIndex > 2) {
                    parsed.put(arg.substring(0, equalsIndex), arg.substring(equalsIndex + 1));
                    continue;
                }
                if (index + 1 < args.length && !args[index + 1].startsWith("--")) {
                    parsed.put(arg, args[++index]);
                }
            }
            return Collections.unmodifiableMap(parsed);
        }
    }

    private static final class Runner {
        private final Config config;
        private final Object lock = new Object();
        private Engine engine;
        private String backendLabel = "unloaded";

        Runner(Config config) {
            this.config = config;
        }

        void initialize() throws Exception {
            Exception lastError = null;
            System.out.println("Resolved model path: " + config.modelPath);
            System.out.println("Resolved cache dir: " + config.cacheDir);
            System.out.println("Java file check: " + Files.isRegularFile(config.modelPath));

            for (String modelPathVariant : buildModelPathVariants(config.modelPath)) {
                for (String backendName : config.backendOrder) {
                    try {
                        System.out.println("Trying LiteRT backend " + backendName.toUpperCase(Locale.US)
                            + " with model path " + modelPathVariant);
                        Backend backend = backendForName(backendName);
                        Engine candidate = new Engine(new EngineConfig(
                            modelPathVariant,
                            backend,
                            null,
                            null,
                            null,
                            config.cacheDir.toString()
                        ));
                        candidate.initialize();
                        engine = candidate;
                        backendLabel = backendName.toUpperCase(Locale.US);
                        return;
                    } catch (Exception exc) {
                        lastError = exc;
                        System.err.println("LiteRT backend " + backendName.toUpperCase(Locale.US)
                            + " failed for path " + modelPathVariant + ": " + exc);
                    }
                }
            }
            throw new IllegalStateException("Unable to initialize LiteRT engine", lastError);
        }

        String getBackendLabel() {
            return backendLabel;
        }

        GenerationResult generate(String prompt) throws Exception {
            synchronized (lock) {
                if (engine == null) {
                    throw new IllegalStateException("LiteRT engine is not initialized");
                }
                try (Conversation conversation = engine.createConversation(new ConversationConfig(
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    GREEDY_SAMPLER,
                    true,
                    null
                ))) {
                    StringBuilder chunks = new StringBuilder();
                    AtomicReference<Throwable> errorRef = new AtomicReference<>();
                    CountDownLatch latch = new CountDownLatch(1);

                    conversation.sendMessageAsync(prompt, new MessageCallback() {
                        @Override
                        public void onMessage(Message message) {
                            String text = message.toString();
                            if (!text.startsWith("<ctrl")) {
                                chunks.append(text);
                            }
                        }

                        @Override
                        public void onDone() {
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            errorRef.set(throwable);
                            latch.countDown();
                        }
                    }, Collections.emptyMap());

                    if (!latch.await(15, TimeUnit.MINUTES)) {
                        throw new IllegalStateException("Timed out waiting for LiteRT response");
                    }
                    Throwable failure = errorRef.get();
                    if (failure != null) {
                        throw new IllegalStateException(
                            failure.getMessage() == null ? "LiteRT generation failed" : failure.getMessage(),
                            failure
                        );
                    }
                    String answer = chunks.toString().trim();
                    if (answer.isEmpty()) {
                        throw new IllegalStateException("LiteRT returned an empty answer");
                    }
                    return new GenerationResult(answer, backendLabel);
                }
            }
        }

        void close() {
            synchronized (lock) {
                if (engine != null) {
                    try {
                        engine.close();
                    } catch (Exception ignored) {
                    }
                    engine = null;
                }
            }
        }

        private Backend backendForName(String backendName) {
            if ("gpu".equalsIgnoreCase(backendName)) {
                return new Backend.GPU();
            }
            return new Backend.CPU(null);
        }

        private List<String> buildModelPathVariants(Path modelPath) {
            LinkedHashSet<String> variants = new LinkedHashSet<>();
            String normalized = modelPath.toString();
            variants.add(normalized);
            if (normalized.indexOf('\\') >= 0) {
                variants.add(normalized.replace('\\', '/'));
            }
            return new ArrayList<>(variants);
        }
    }

    private static final class GenerationResult {
        final String text;
        final String backend;

        GenerationResult(String text, String backend) {
            this.text = text;
            this.backend = backend;
        }
    }

    private static final class HealthHandler implements HttpHandler {
        private final Config config;
        private final Runner runner;

        HealthHandler(Config config, Runner runner) {
            this.config = config;
            this.runner = runner;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JsonObject payload = new JsonObject();
            payload.addProperty("status", "ok");
            payload.addProperty("model", config.modelId);
            payload.addProperty("model_path", config.modelPath.toString());
            payload.addProperty("backend", runner.getBackendLabel());
            writeJson(exchange, 200, payload);
        }
    }

    private static final class ModelsHandler implements HttpHandler {
        private final Config config;

        ModelsHandler(Config config) {
            this.config = config;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JsonObject payload = new JsonObject();
            payload.addProperty("object", "list");
            JsonArray data = new JsonArray();
            JsonObject model = new JsonObject();
            model.addProperty("id", config.modelId);
            model.addProperty("object", "model");
            model.addProperty("owned_by", "litert-host");
            data.add(model);
            payload.add("data", data);
            writeJson(exchange, 200, payload);
        }
    }

    private static final class CompletionsHandler implements HttpHandler {
        private final Config config;
        private final Runner runner;

        CompletionsHandler(Config config, Runner runner) {
            this.config = config;
            this.runner = runner;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                JsonObject request = JsonParser.parseString(readBody(exchange.getRequestBody())).getAsJsonObject();
                String prompt = extractPrompt(request);
                GenerationResult result = runner.generate(prompt);

                JsonObject payload = new JsonObject();
                payload.addProperty("id", "chatcmpl-" + UUID.randomUUID());
                payload.addProperty("object", "chat.completion");
                payload.addProperty("created", Instant.now().getEpochSecond());
                payload.addProperty("model", config.modelId);

                JsonArray choices = new JsonArray();
                JsonObject choice = new JsonObject();
                choice.addProperty("index", 0);
                JsonObject message = new JsonObject();
                message.addProperty("role", "assistant");
                message.addProperty("content", result.text);
                choice.add("message", message);
                choice.addProperty("finish_reason", "stop");
                choices.add(choice);
                payload.add("choices", choices);

                JsonObject usage = new JsonObject();
                usage.addProperty("prompt_tokens", 0);
                usage.addProperty("completion_tokens", 0);
                usage.addProperty("total_tokens", 0);
                payload.add("usage", usage);
                payload.addProperty("backend", result.backend);

                writeJson(exchange, 200, payload);
            } catch (Exception exc) {
                JsonObject error = new JsonObject();
                JsonObject body = new JsonObject();
                body.addProperty("message", exc.toString());
                body.addProperty("type", exc.getClass().getSimpleName());
                error.add("error", body);
                writeJson(exchange, 500, error);
            }
        }
    }

    private static String extractPrompt(JsonObject request) {
        JsonArray messages = request.getAsJsonArray("messages");
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Expected a non-empty messages array");
        }
        ArrayList<String> parts = new ArrayList<>();
        for (JsonElement element : messages) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject message = element.getAsJsonObject();
            String role = getString(message, "role", "user");
            String content = flattenContent(message.get("content"));
            if (content.isBlank()) {
                continue;
            }
            if ("user".equalsIgnoreCase(role)) {
                parts.add(content);
            } else {
                parts.add(role.toUpperCase(Locale.US) + ":\n" + content);
            }
        }
        String prompt = String.join("\n\n", parts).trim();
        if (prompt.isEmpty()) {
            throw new IllegalArgumentException("No prompt text found in request");
        }
        return prompt;
    }

    private static String flattenContent(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (element.isJsonArray()) {
            ArrayList<String> parts = new ArrayList<>();
            for (JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    parts.add(item.getAsString());
                    continue;
                }
                if (!item.isJsonObject()) {
                    continue;
                }
                JsonObject object = item.getAsJsonObject();
                if ("text".equalsIgnoreCase(getString(object, "type", ""))) {
                    String text = getString(object, "text", "");
                    if (!text.isBlank()) {
                        parts.add(text);
                    }
                }
            }
            return String.join("\n", parts);
        }
        return "";
    }

    private static String getString(JsonObject object, String key, String fallback) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? fallback : value.getAsString();
    }

    private static String readBody(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void writeJson(HttpExchange exchange, int statusCode, JsonObject payload) throws IOException {
        byte[] body = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        } finally {
            exchange.close();
        }
    }
}
