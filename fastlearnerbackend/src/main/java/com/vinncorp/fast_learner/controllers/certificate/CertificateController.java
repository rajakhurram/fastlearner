package com.vinncorp.fast_learner.controllers.certificate;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.controllers.youtube_video.user.CertificateResponse;
import com.vinncorp.fast_learner.services.certificate.ICertificateService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(APIUrls.CERTIFICATE)
@RequiredArgsConstructor
public class CertificateController {

    private final ICertificateService service;

    @GetMapping(APIUrls.GET_CERTIFICATE)
    public ResponseEntity<Message<CertificateResponse>> generateCertificate(@PathVariable Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException {
        var m = service.generateCertificate(courseId, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @GetMapping(APIUrls.VERIFY_CERTIFICATE)
    public ResponseEntity<byte[]> verifyCertificate(@PathVariable String certificateId)
            throws BadRequestException, EntityNotFoundException, IOException {
        return service.downloadCertificate(null, false, certificateId, null);
    }

    @GetMapping(APIUrls.VERIFY_CERTIFICATE_FOR_RESPONSE)
    public ResponseEntity<Message<String>> verifyCertificateForResponse(@PathVariable String certificateId)
            throws BadRequestException {
        return ResponseEntity.ok(service.verifyCertificate(certificateId));
    }

    @GetMapping(APIUrls.DOWNLOAD_CERTIFICATE)
    public ResponseEntity<byte[]> downloadCertificate(@RequestParam Long courseId, @RequestParam Boolean isDownloadable, Principal principal)
            throws BadRequestException, EntityNotFoundException, IOException {
        return service.downloadCertificate(courseId, isDownloadable, null, principal);
    }
}
