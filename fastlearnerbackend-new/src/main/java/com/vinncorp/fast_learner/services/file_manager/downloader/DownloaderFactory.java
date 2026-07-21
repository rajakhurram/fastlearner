package com.vinncorp.fast_learner.services.file_manager.downloader;

import com.vinncorp.fast_learner.util.enums.FileType;
import org.springframework.http.ResponseEntity;

public interface DownloaderFactory {
    ResponseEntity<byte[]> download(String filename, FileType fileType, String email);
}
