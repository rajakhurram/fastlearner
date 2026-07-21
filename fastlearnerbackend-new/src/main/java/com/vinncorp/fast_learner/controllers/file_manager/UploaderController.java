package com.vinncorp.fast_learner.controllers.file_manager;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.request.uploader.ResourceDeleteRequest;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.services.file_manager.uploader.UploaderFactory;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.enums.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Objects;

@RestController
@RequestMapping(APIUrls.UPLOADER)
public class UploaderController {

    @Value("${multipart.max-file-size}")
    private long MAX_FILE_SIZE;

    @Autowired
    @Qualifier(value = "gcpUploader" /*"localUploader"*/)
    private UploaderFactory uploaderFactory;

    @PostMapping(value = APIUrls.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Message<Object>> upload(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("fileType") String fileType,
            Principal principal)
            throws BadRequestException, UnsupportedEncodingException {

        if(Objects.isNull(file))
            throw new BadRequestException("Please upload a valid file.");
        if(!file.isEmpty())
            if(file.getSize() > MAX_FILE_SIZE * 1024 * 1024)
                throw new BadRequestException("File size exceeds the maximum allowed limit of "+MAX_FILE_SIZE+" MB.");
        UploadFileRequest request = new UploadFileRequest();
        request.setFile(file);
        FileType fileType1 = FileType.fromString(fileType);
        if (fileType1 == null) {
            throw new BadRequestException("File type is not valid.");
        }
        request.setFileType(fileType1);
        Message<Object> m = new Message<>()
                .setStatus(HttpStatus.CREATED.value())
                .setCode(HttpStatus.CREATED.name())
                .setMessage("Saved the file successfully.")
                .setData(uploaderFactory.upload(request, principal.getName()));
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(value = APIUrls.UPLOAD_FOR_REGENERATION)
    public ResponseEntity<Message<Object>> uploadForRegeneration(
            @RequestParam("fileType") String fileType,
            @RequestParam(value = "url", required = true) String url,
            Principal principal)
            throws BadRequestException, UnsupportedEncodingException {
        UploadFileRequest request = new UploadFileRequest();
        FileType fileType1 = FileType.fromString(fileType);
        if (fileType1 == null) {
            throw new BadRequestException("File type is not valid.");
        }
        request.setFileType(fileType1);
        request.setUrl(url);
        Message<Object> m = new Message<>()
                .setStatus(HttpStatus.CREATED.value())
                .setCode(HttpStatus.CREATED.name())
                .setMessage("Saved the file successfully.")
                .setData(uploaderFactory.upload(request, principal.getName()));
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_RESOURCE)
    public ResponseEntity deleteResource(@RequestBody ResourceDeleteRequest request, Principal principal) {
        uploaderFactory.delete(request.getId(), request.getUrl(), request.getFileType(), request.getTopicId());
        return ResponseEntity.ok(null);
    }
}
