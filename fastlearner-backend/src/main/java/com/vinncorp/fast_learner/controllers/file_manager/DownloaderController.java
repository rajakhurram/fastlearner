package com.vinncorp.fast_learner.controllers.file_manager;

import com.vinncorp.fast_learner.services.file_manager.downloader.DownloaderFactory;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.enums.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.DOWNLOADER)
public class DownloaderController {

    @Autowired
    @Qualifier(value = "gcpDownloader")
    private DownloaderFactory downloaderFactory;

    @GetMapping(APIUrls.DOWNLOAD)
    public ResponseEntity<byte[]> download(
            @RequestParam String filename,
            @RequestParam FileType fileType,
            Principal principal) {
        MediaType mediaType = null;

        switch (fileType) {
            case VIDEO:
            case PREVIEW_VIDEO:
                mediaType = MediaType.valueOf("video/mp4");
                break;
            case PROFILE_IMAGE:
            case PREVIEW_THUMBNAIL:
                mediaType = MediaType.valueOf(MediaType.IMAGE_JPEG_VALUE);
        }
        try {

            return downloaderFactory.download(filename, fileType, null);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
