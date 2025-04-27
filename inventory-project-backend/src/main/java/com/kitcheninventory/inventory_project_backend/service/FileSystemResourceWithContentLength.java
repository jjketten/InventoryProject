package com.kitcheninventory.inventory_project_backend.service;

import org.springframework.core.io.FileSystemResource;
import java.io.File;

public class FileSystemResourceWithContentLength extends FileSystemResource {
    private final long contentLength;

    public FileSystemResourceWithContentLength(File file) {
        super(file);
        this.contentLength = file.length();
    }

    @Override
    public long contentLength() {
        return this.contentLength;
    }
}

