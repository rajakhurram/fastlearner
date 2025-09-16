package com.vinncorp.fast_learner.services.certificate;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.controllers.youtube_video.user.CertificateResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.security.Principal;

public interface ICertificateService {
    Message<CertificateResponse> generateCertificate(Long courseId, String email) throws EntityNotFoundException, BadRequestException;

    Message<String> verifyCertificate(String certificateId) throws BadRequestException;

    ResponseEntity<byte[]> downloadCertificate(@PathVariable Long courseId, Boolean isDownloadable, String uuid, Principal principal) throws BadRequestException, EntityNotFoundException, IOException, IOException;
}
