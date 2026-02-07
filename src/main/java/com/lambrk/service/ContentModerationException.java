package com.lambrk.service;

import java.util.List;

public class ContentModerationException extends RuntimeException {
    
    private final List<String> violationCategories;

    public ContentModerationException(String message, List<String> violationCategories) {
        super(message);
        this.violationCategories = violationCategories;
    }

    public List<String> getViolationCategories() {
        return violationCategories;
    }
}
