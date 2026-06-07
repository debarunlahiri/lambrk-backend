package com.lambrk.filewatcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import org.springframework.context.ApplicationEvent;

public class FileChangeEvent extends ApplicationEvent {

  private final Path filePath;
  private final WatchEvent.Kind<?> eventKind;

  public FileChangeEvent(Object source, Path filePath, WatchEvent.Kind<?> eventKind) {
    super(source);
    this.filePath = filePath;
    this.eventKind = eventKind;
  }

  public Path getFilePath() {
    return filePath;
  }

  public WatchEvent.Kind<?> getEventKind() {
    return eventKind;
  }

  public boolean isCreate() {
    return eventKind == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
  }

  public boolean isModify() {
    return eventKind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
  }

  public boolean isDelete() {
    return eventKind == java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
  }

  @Override
  public String toString() {
    return String.format(
        "FileChangeEvent{path=%s, kind=%s, time=%d}", filePath, eventKind, getTimestamp());
  }
}
