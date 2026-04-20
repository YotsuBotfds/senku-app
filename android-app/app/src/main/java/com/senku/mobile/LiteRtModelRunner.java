package com.senku.mobile;

import android.content.Context;
import android.os.Build;

import com.google.ai.edge.litertlm.Backend;
import com.google.ai.edge.litertlm.Content;
import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.EngineConfig;
import com.google.ai.edge.litertlm.Message;
import com.google.ai.edge.litertlm.MessageCallback;
import com.google.ai.edge.litertlm.SamplerConfig;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class LiteRtModelRunner {
    private static final SamplerConfig GREEDY_SAMPLER = new SamplerConfig(1, 1.0, 0.0, 0);
    private static Engine engine;
    private static String loadedModelPath;
    private static Integer loadedMaxTokens;
    private static String loadedBackendLabel = "unloaded";

    private LiteRtModelRunner() {
    }

    public interface PartialResultListener {
        void onPartialText(String partialText);
    }

    public static synchronized void close() {
        if (engine != null) {
            try {
                engine.close();
            } catch (Exception ignored) {
            }
        }
        engine = null;
        loadedModelPath = null;
        loadedMaxTokens = null;
        loadedBackendLabel = "unloaded";
    }

    public static synchronized String getLoadedBackendLabel() {
        return loadedBackendLabel;
    }

    public static synchronized String generate(Context context, String modelPath, String prompt, Integer maxTokens) throws Exception {
        return generateStreaming(context, modelPath, prompt, maxTokens, null);
    }

    public static synchronized String generateStreaming(
        Context context,
        String modelPath,
        String prompt,
        Integer maxTokens,
        PartialResultListener listener
    ) throws Exception {
        Engine loadedEngine = ensureEngine(context, modelPath, maxTokens);
        Conversation conversation = loadedEngine.createConversation(
            new ConversationConfig(
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                GREEDY_SAMPLER
            )
        );

        StringBuilder chunks = new StringBuilder();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            conversation.sendMessageAsync(
                com.google.ai.edge.litertlm.Contents.Companion.of(new Content.Text(prompt)),
                new MessageCallback() {
                    @Override
                    public void onMessage(Message message) {
                        String text = sanitizeModelText(message.toString());
                        if (!text.isEmpty()) {
                            appendStreamingChunk(chunks, text);
                            if (listener != null) {
                                listener.onPartialText(chunks.toString());
                            }
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
                },
                Collections.emptyMap()
            );

            if (!latch.await(15, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Timed out waiting for the on-device model response");
            }
            Throwable failure = errorRef.get();
            if (failure != null) {
                throw new IllegalStateException(
                    failure.getMessage() == null ? "On-device generation failed" : failure.getMessage(),
                    failure
                );
            }
            return chunks.toString().trim();
        } finally {
            try {
                conversation.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void appendStreamingChunk(StringBuilder chunks, String text) {
        if (chunks == null || text == null || text.isEmpty()) {
            return;
        }
        String current = chunks.toString();
        if (text.equals(current)) {
            return;
        }
        if (!current.isEmpty() && text.startsWith(current)) {
            chunks.setLength(0);
            chunks.append(text);
            return;
        }
        chunks.append(text);
    }

    private static String sanitizeModelText(String text) {
        String cleaned = text == null ? "" : text;
        String trimmed = cleaned.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String lower = trimmed.toLowerCase(Locale.US);
        if (lower.startsWith("<ctrl")) {
            return "";
        }
        cleaned = cleaned.replaceAll("(?i)<pad>", "");
        cleaned = cleaned.replaceAll("(?i)<bos>", "");
        cleaned = cleaned.replaceAll("(?i)</bos>", "");
        cleaned = cleaned.replaceAll("(?i)<eos>", "");
        cleaned = cleaned.replaceAll("(?i)</eos>", "");
        cleaned = cleaned.replaceAll("(?i)<unk>", "");
        cleaned = cleaned.replaceAll("(?i)<s>", "");
        cleaned = cleaned.replaceAll("(?i)</s>", "");
        if (cleaned.trim().isEmpty()) {
            return "";
        }
        return cleaned;
    }

    private static synchronized Engine ensureEngine(Context context, String modelPath, Integer maxTokens) throws Exception {
        boolean sameTokenBudget = maxTokens == null
            ? loadedMaxTokens == null
            : maxTokens.equals(loadedMaxTokens);
        if (engine != null && modelPath.equals(loadedModelPath) && sameTokenBudget) {
            return engine;
        }

        close();

        Exception lastError = null;
        boolean preferCpu = shouldPreferCpuBackend();
        Backend[] backends = preferCpu
            ? new Backend[]{new Backend.CPU()}
            : new Backend[]{new Backend.GPU(), new Backend.CPU()};
        String[] labels = preferCpu
            ? new String[]{"CPU"}
            : new String[]{"GPU", "CPU"};
        for (int index = 0; index < backends.length; index++) {
            try {
                String cacheDir = prepareCacheDirectory(context, modelPath, maxTokens, preferCpu).getAbsolutePath();
                Engine candidate = new Engine(
                    new EngineConfig(
                        modelPath,
                        backends[index],
                        null,
                        null,
                        maxTokens,
                        cacheDir
                    )
                );
                candidate.initialize();
                engine = candidate;
                loadedModelPath = modelPath;
                loadedMaxTokens = maxTokens;
                loadedBackendLabel = labels[index];
                return candidate;
            } catch (Exception error) {
                lastError = error;
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new IllegalStateException("Unable to initialize imported model");
    }

    private static boolean shouldPreferCpuBackend() {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abi != null && abi.toLowerCase().contains("x86")) {
                return true;
            }
        }
        String fingerprint = Build.FINGERPRINT == null ? "" : Build.FINGERPRINT.toLowerCase();
        String hardware = Build.HARDWARE == null ? "" : Build.HARDWARE.toLowerCase();
        return fingerprint.contains("generic")
            || hardware.contains("ranchu")
            || hardware.contains("goldfish");
    }

    private static File prepareCacheDirectory(Context context, String modelPath, Integer maxTokens, boolean preferCpu) {
        File baseDir = new File(context.getCacheDir(), "litert");
        File modelDir = new File(baseDir, sanitizeModelName(modelPath) + "-" + (maxTokens == null ? "default" : maxTokens));
        if (!modelDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            modelDir.mkdirs();
        }
        if (preferCpu) {
            clearXnnpackCacheFiles(modelDir);
        }
        return modelDir;
    }

    private static void clearXnnpackCacheFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (name.contains("xnnpack") || name.endsWith(".cache")) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    private static String sanitizeModelName(String modelPath) {
        String fileName = new File(modelPath).getName().toLowerCase();
        String sanitized = fileName.replaceAll("[^a-z0-9._-]+", "_");
        return sanitized.isEmpty() ? "model" : sanitized;
    }
}
