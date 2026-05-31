package com.lambrk.filewatcher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileWatcherService {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);

    private final FileWatcherProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    private WatchService watchService;
    private ExecutorService executor;
    private final Map<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public FileWatcherService(FileWatcherProperties properties, ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        if (!properties.isEnabled() || properties.getDirectories().isEmpty()) {
            logger.info("File watcher is disabled or no directories configured");
            return;
        }
        startWatching();
    }

    public void startWatching() {
        if (running.compareAndSet(false, true)) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                executor = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "file-watcher");
                    t.setDaemon(true);
                    return t;
                });

                for (String dir : properties.getDirectories()) {
                    Path path = Paths.get(dir);
                    if (!Files.exists(path)) {
                        logger.warn("Watched directory does not exist, creating: {}", path);
                        Files.createDirectories(path);
                    }
                    registerDirectory(path);
                }

                executor.submit(this::watchLoop);
                logger.info("File watcher started for {} directories", properties.getDirectories().size());
            } catch (IOException e) {
                logger.error("Failed to start file watcher", e);
                running.set(false);
            }
        }
    }

    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE);
        watchKeys.put(key, dir);
        logger.debug("Registered watch on directory: {}", dir);

        if (properties.isRecursive()) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path subDir, BasicFileAttributes attrs) throws IOException {
                    if (!subDir.equals(dir)) {
                        WatchKey subKey = subDir.register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE);
                        watchKeys.put(subKey, subDir);
                        logger.debug("Registered recursive watch on subdirectory: {}", subDir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void watchLoop() {
        while (running.get()) {
            try {
                WatchKey key = watchService.poll(properties.getPollIntervalMs(), TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }

                Path dir = watchKeys.get(key);
                if (dir == null) {
                    logger.warn("Received event for unknown watch key");
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.warn("Watch event overflow occurred in {}", dir);
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    Path fullPath = dir.resolve(fileName);

                    logger.debug("File event: {} on {}", kind, fullPath);

                    if (properties.isEmitSpringEvents()) {
                        publishEvent(fullPath, kind);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    watchKeys.remove(key);
                    logger.info("Watch key removed for directory: {}", dir);
                    if (watchKeys.isEmpty()) {
                        logger.warn("No more directories being watched, stopping file watcher");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("File watcher interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error in file watch loop", e);
            }
        }
    }

    @Async
    private void publishEvent(Path filePath, WatchEvent.Kind<?> kind) {
        try {
            FileChangeEvent event = new FileChangeEvent(this, filePath, kind);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            logger.error("Failed to publish file change event for {}", filePath, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            logger.info("Shutting down file watcher");
            if (executor != null) {
                executor.shutdownNow();
            }
            if (watchService != null) {
                try {
                    watchService.close();
                } catch (IOException e) {
                    logger.error("Error closing watch service", e);
                }
            }
            watchKeys.clear();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public Map<WatchKey, Path> getWatchedDirectories() {
        return Map.copyOf(watchKeys);
    }
}
