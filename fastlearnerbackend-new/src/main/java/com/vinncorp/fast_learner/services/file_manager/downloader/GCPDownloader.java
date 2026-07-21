package com.vinncorp.fast_learner.services.file_manager.downloader;

import com.vinncorp.fast_learner.util.enums.FileType;
import com.vinncorp.fast_learner.util.gcp.DataBucketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service("gcpDownloader")
@RequiredArgsConstructor
public class GCPDownloader implements DownloaderFactory{

    private final DataBucketUtil dataBucketUtil;

    @Override
    public ResponseEntity<byte[]> download(String filename, FileType fileType, String email) {
        log.info("Loading file: " + filename);
        return dataBucketUtil.download(fileType+"/"+filename, fileType);
    }
}
