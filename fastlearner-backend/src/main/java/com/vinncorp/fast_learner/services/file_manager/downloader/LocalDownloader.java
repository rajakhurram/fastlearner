package com.vinncorp.fast_learner.services.file_manager.downloader;


import com.vinncorp.fast_learner.util.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalDownloader implements DownloaderFactory{

    @Value("${directory.root}")
    private String ROOT_DIRECTORY;

    @Value("${directory.courses}")
    private String COURSES_DIRECTORY;

    @Value("${directory.users}")
    private String USERS_DIRECTORY;

    @Override
    public ResponseEntity<byte[]> download(String filename, FileType fileType, String email) {
        log.info("Loading file: " + filename);
        Path directory = null;
        switch (fileType) {
            case VIDEO:
            case PREVIEW_VIDEO:
            case ARTICLE:
            case PREVIEW_THUMBNAIL:
            case TRANSCRIBE:
                directory = Paths.get(ROOT_DIRECTORY + COURSES_DIRECTORY);
                break;
            case PROFILE_IMAGE:
                directory = Paths.get(ROOT_DIRECTORY + USERS_DIRECTORY);
                break;
        }
        try {
            // Read the file content into a byte array
            byte[] fileContent = Files.readAllBytes(directory.resolve(filename));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
