package com.vinncorp.fast_learner.dtos.file_manager.uploader.gcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {

    private String fileName;
    private String fileUrl;
    private String previewVideoVttContent;
}