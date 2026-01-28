package com.vinncorp.fast_learner.services.file_manager.uploader;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;

import java.io.UnsupportedEncodingException;

public interface UploaderFactory {

    Object upload(UploadFileRequest request, String email) throws BadRequestException, UnsupportedEncodingException;

    void delete(Long id, String url, String fileType, Long topicId);
}
