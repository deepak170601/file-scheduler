package com.example.JsonProcessing.service;

import com.example.JsonProcessing.model.JsonData;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JsonProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(JsonProcessingService.class);
    private final Gson gson = new Gson();

    // Global map to hold processors for each reference identifier.
    private final Map<String, RefProcessor> refProcessors = new ConcurrentHashMap<>();
    // Global map to track remaining file count per refId.
    private final Map<String, AtomicInteger> remainingCountMap = new ConcurrentHashMap<>();
    // Scheduled executor used to schedule timers for each refId.
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    /**
     * Starts the file watcher in a new thread to monitor the given JSON folder.
     * When a new JSON file is created, it is processed and then moved to the archive folder.
     */
    public void initializeAndWatch(String folderPath, String archiveFolderPath) {
        new Thread(() -> startFileWatcher(folderPath, archiveFolderPath)).start();
    }

    /**
     * Starts a file watcher that monitors the given JSON folder. When a new file is created,
     * it is processed and then moved to the archive folder.
     *
     * @param folderPath        the folder to watch for JSON files.
     * @param archiveFolderPath the folder where processed files will be moved (archived).
     */
    public void startFileWatcher(String folderPath, String archiveFolderPath) {
        Path folder = Paths.get(folderPath);
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            folder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            logger.info("Started watching folder: {}", folderPath);
            while (true) {
                WatchKey key = watchService.take(); // Wait for events.
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    if (fileName.toString().endsWith(".json")) {
                        Path fullPath = folder.resolve(fileName);
                        processAndArchiveFile(fullPath, archiveFolderPath);
                    }
                }
                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error in file watcher", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Reads, processes, and archives a JSON file.
     * The file is read, parsed into a JsonData object, and processed based on its jobInfo.referenceIdentifier.
     * Afterwards, the file is moved to the archive folder.

     */
    private void processAndArchiveFile(Path filePath, String archiveFolderPath) {
        int maxRetries = 3;
        JsonData data = null;
        String content = null;
        boolean success = false;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                logger.info("Processing file: {} (Attempt {})", filePath, attempt);
                data = gson.fromJson(content, JsonData.class);
                if (data == null || data.getJobInfo() == null) {
                    logger.warn("Parsed JsonData is null or missing jobInfo on attempt {} for file: {}",
                            attempt, filePath);
                    Thread.sleep(2000);
                } else {
                    success = true;
                    break;
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Error reading file {} on attempt {}: {}", filePath, attempt, e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        if (!success) {
            logger.error("Failed to process file {} after {} attempts. Content (if any): {}", filePath, maxRetries, content);
            return;
        }
        String refId = data.getJobInfo().getReferenceIdentifier();
        if (refId == null) {
            logger.error("Reference Identifier is null for file: {}", filePath);
            return;
        }
        // Initialize or update the global remaining count map.
        JsonData finalData1 = data;
        remainingCountMap.computeIfAbsent(refId, k -> new AtomicInteger(finalData1.getJobInfo().getTransactionCount()));
        // Get or create a processor for this reference identifier.
        JsonData finalData = data;
        RefProcessor processor = refProcessors.computeIfAbsent(refId, k -> {
            logger.info("Creating new processor for referenceIdentifier: {}", k);
            int expectedCount = finalData.getJobInfo().getTransactionCount();
            return new RefProcessor(refId, expectedCount, scheduler, () -> refProcessors.remove(refId));
        });
        processor.processFile(data);
        // Move the file to the archive folder.
        try {
            Path archiveFolder = Paths.get(archiveFolderPath);
            if (!Files.exists(archiveFolder)) {
                Files.createDirectories(archiveFolder);
            }
            Path targetPath = archiveFolder.resolve(filePath.getFileName());
            Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Archived file: {} to {}", filePath, targetPath);
        } catch (IOException e) {
            logger.error("Error archiving file: " + filePath, e);
        }
    }

    private class RefProcessor {
        private final String referenceIdentifier;
        private final ScheduledExecutorService scheduler;
        private final Runnable onFinishCallback;
        private ScheduledFuture<?> scheduledFuture;
        // List to store received ray information for this reference.
        private final List<RayInfo> records = new ArrayList<>();
        private final Logger logger = LoggerFactory.getLogger(RefProcessor.class);

        public RefProcessor(String referenceIdentifier, int expectedTransactionCount,
                            ScheduledExecutorService scheduler, Runnable onFinishCallback) {
            this.referenceIdentifier = referenceIdentifier;
            this.scheduler = scheduler;
            this.onFinishCallback = onFinishCallback;
            scheduleTimer();
            logger.info("Initialized RefProcessor for refId: {} with expected transaction count: {}",
                    referenceIdentifier, expectedTransactionCount);
        }

        /**
         * Processes an incoming JsonData file by adding its ray information.
         * Resets the timer, and decrements the global remaining count.
         * If the remaining count becomes zero, finish() is invoked immediately.
         *
         */
        public synchronized void processFile(JsonData data) {
            String rayId = data.getTrackingInfo().getRayIdentifier();
            String state = data.getTrackingInfo().getState();
            records.add(new RayInfo(rayId, state));
            logger.info("Received file for refId: {}: rayId: {}, state: {}. Total records: {}",
                    referenceIdentifier, rayId, state, records.size());
            // Decrement the global remaining count.
            AtomicInteger counter = remainingCountMap.get(referenceIdentifier);
            if (counter != null) {
                int remaining = counter.decrementAndGet();
                logger.info("Remaining count for {} is {}", referenceIdentifier, remaining);
                if (remaining <= 0) {
                    finish();
                    remainingCountMap.remove(referenceIdentifier);
                    return;
                }
            }
            // Cancel and reset the timer.
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            scheduleTimer();
        }

        private void scheduleTimer() {
            scheduledFuture = scheduler.schedule(this::finish, 1, TimeUnit.MINUTES);
            logger.info("Scheduled finish timer for refId: {} in 5 minutes", referenceIdentifier);
        }


        public synchronized void finish() {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            if (records.isEmpty()) {
                logger.info("No records to process for refId: {}", referenceIdentifier);
                return;
            }
            Map<String, String> sortedRayData = new TreeMap<>();
            for (RayInfo info : records) {
                sortedRayData.put(info.getRayId(), info.getState());
            }
            logger.info("Final grouped output for Reference Identifier: {}", referenceIdentifier);
            int counter = 1;
            for (Map.Entry<String, String> entry : sortedRayData.entrySet()) {
                logger.info("{}. Ray Identifier: {} | State: {}", counter++, entry.getKey(), entry.getValue());
            }
            logger.info("Total ray_ids processed for refId {}: {}", referenceIdentifier, records.size());
            records.clear();
            onFinishCallback.run();
            logger.info("Finished processing for refId: {}", referenceIdentifier);
        }
    }


    private static class RayInfo {
        private final String rayId;
        private final String state;

        public RayInfo(String rayId, String state) {
            this.rayId = rayId;
            this.state = state;
        }

        public String getRayId() {
            return rayId;
        }

        public String getState() {
            return state;
        }
    }
}
