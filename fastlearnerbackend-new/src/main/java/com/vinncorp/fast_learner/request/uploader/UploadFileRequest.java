package com.vinncorp.fast_learner.request.uploader;

import com.vinncorp.fast_learner.util.enums.FileType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileRequest {
    private MultipartFile file;
    private FileType fileType;
    private String url;
}
