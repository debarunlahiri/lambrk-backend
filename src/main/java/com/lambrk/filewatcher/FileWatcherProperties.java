package com.lambrk.filewatcher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "lambrk.file-watcher")
public class FileWatcherProperties {

    private boolean enabled = true;
    private List<String> directories = new ArrayList<>();
    private boolean recursive = false;
    private long pollIntervalMs = 1000;
    private boolean emitSpringEvents = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<String> getDirectories() { return directories; }
    public void setDirectories(List<String> directories) { this.directories = directories; }

    public boolean isRecursive() { return recursive; }
    public void setRecursive(boolean recursive) { this.recursive = recursive; }

    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public boolean isEmitSpringEvents() { return emitSpringEvents; }
    public void setEmitSpringEvents(boolean emitSpringEvents) { this.emitSpringEvents = emitSpringEvents; }
}
