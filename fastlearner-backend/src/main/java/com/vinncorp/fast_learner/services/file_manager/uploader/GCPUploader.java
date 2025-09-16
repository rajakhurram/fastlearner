package com.vinncorp.fast_learner.services.file_manager.uploader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;
import com.vinncorp.fast_learner.dtos.file_manager.uploader.gcp.FileDto;
import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import com.vinncorp.fast_learner.response.docs.DocumentUploaderResponse;
import com.vinncorp.fast_learner.response.video.VideoUploaderResponse;
import com.vinncorp.fast_learner.services.docs.IDocumentService;
import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.util.enums.FileType;
import com.vinncorp.fast_learner.util.gcp.DataBucketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Service("gcpUploader")
@RequiredArgsConstructor
public class GCPUploader implements UploaderFactory{

    @Value("${gcp.bucket.url}")
    private String GCP_BUCKET_URL;

    private final Storage storage;
    private final DataBucketUtil dataBucketUtil;
    private final IVideoService videoService;
    private final IDocumentService documentService;
    private final CourseRepository courseRepo;
    private final IUserCourseProgressService userCourseProgressService;

    @Override
    public Object upload(UploadFileRequest request, String email) throws BadRequestException, UnsupportedEncodingException {
        if (Objects.isNull(request.getFile()) && Objects.nonNull(request.getUrl())) {
            return regenerateSummary(request.getUrl(), request.getFileType());
        }
        log.info("Uploading the file resource into Google Cloud bucket.");
        String originalFilename = request.getFile().getOriginalFilename();

        if (Objects.isNull(originalFilename)) {
            throw new BadRequestException("Original file name is null");
        }

        Path path = new File(originalFilename).toPath();
        FileDto fileDto = null;

        try {
            String contentType = Files.probeContentType(path);
            String alteredFileName = RandomStringUtils.randomAlphanumeric(8) + "_" + originalFilename.replace(" ", "_");

            // Generate transcript or summary before the file upload
            if (request.getFileType() == FileType.PREVIEW_VIDEO || request.getFileType() == FileType.VIDEO) {
                VideoTranscript videoTranscript = videoService.generateTranscript(request.getFile());

                if (request.getFileType() == FileType.PREVIEW_VIDEO) {
                    fileDto = dataBucketUtil.uploadFile(request, alteredFileName, contentType);
                    fileDto.setPreviewVideoVttContent(videoTranscript.getVttContent());
                    return fileDto;
                } else if (request.getFileType() == FileType.VIDEO) {
                    // Proceed to file upload after generating transcript
                    fileDto = dataBucketUtil.uploadFile(request, alteredFileName, contentType);
                    return VideoUploaderResponse.builder()
                            .transcriptData(videoTranscript)
                            .url(fileDto.getFileUrl())
                            .fileName(originalFilename)
                            .build();
                }
            } else if (request.getFileType() == FileType.DOCS) {
                if (!originalFilename.contains(".pdf")) {
                    throw new BadRequestException("Please only provide PDF documents.");
                }

                // Generate document summary before file upload
                DocumentSummaryResponse documentSummary = documentService.generateSummary(request.getFile());

                // Proceed to file upload after generating document summary
                fileDto = dataBucketUtil.uploadFile(request, alteredFileName, contentType);
                return DocumentUploaderResponse.builder()
                        .fileName(originalFilename)
                        .url(fileDto.getFileUrl())
                        .summary(documentSummary.getData().getSummary())
                        .build();
            }

            // If none of the conditions matched, proceed with normal file upload
            fileDto = dataBucketUtil.uploadFile(request, alteredFileName, contentType);

            if (fileDto != null) {
                log.debug("File uploaded successfully, file name: " + fileDto.getFileName() + " and URL: " + fileDto.getFileUrl());
                return fileDto.getFileUrl();
            }

        }catch (Exception e) {
            String rawMessage = e.getMessage();
            String extractedMessage = rawMessage;

            try {
                int start = rawMessage.indexOf('{');
                int end = rawMessage.lastIndexOf('}');
                if (start != -1 && end != -1 && end > start) {
                    String jsonPart = rawMessage.substring(start, end + 1);

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(jsonPart);
                    extractedMessage = root.path("message").asText();
                }
            } catch (Exception parseEx) {
            }

            throw new BadRequestException("Error occurred while uploading: " + extractedMessage);
        }

        return null;
    }

    private Object regenerateSummary(String url, FileType fileType) throws BadRequestException {
        log.info("Regenerating the summary for "+ fileType.name());
        if (fileType == FileType.VIDEO) {
            // call to generate the transcript of the video.
            VideoTranscript videoTranscript = videoService.generateTranscript(dataBucketUtil.downloadFileOrVideo(url, fileType));
            return VideoUploaderResponse.builder()
                    .transcriptData(videoTranscript)
                    .url(url)
                    .fileName(url.split("/VIDEO/")[1])
                    .build();
        } else if (fileType == FileType.DOCS) {
            if (!url.contains(".pdf")) {
                throw new BadRequestException("Please only provide pdf document.");
            }
            DocumentSummaryResponse documentSummary = documentService.generateSummary(dataBucketUtil.downloadFileOrVideo(url, fileType));
            return DocumentUploaderResponse.builder()
                    .fileName(url.split("/DOCS/")[1])
                    .url(url)
                    .summary(documentSummary.getData().getSummary())
                    .build();
        }
        throw new BadRequestException("You provide wrong values.");
    }

    @Override
    public void delete(Long id, String url, String fileType, Long topicId) {
        FileType filetype = FileType.valueOf(fileType);
        if(Objects.isNull(filetype))
            return;
        if(Objects.isNull(id)) {
            switch (filetype) {
                case PREVIEW_THUMBNAIL, PREVIEW_VIDEO -> {
                    if (courseRepo.existsByThumbnailOrPreviewVideoURL(url, url))
                        return;
                }
                case DOCS, ARTICLE -> {
                    if (documentService.existsByUrl(url))
                        return;
                }
                case VIDEO -> {
                    if (videoService.existsByUrl(url))
                        return;
                }
            }
        }else{
            switch (filetype) {
                case DOCS, ARTICLE -> { // Document and Article should be deleted from db.
                    documentService.deleteById(id);
                    userCourseProgressService.deleteAllUserCourseProgressOfVideo(topicId);
                }
                case VIDEO -> { // Video should be deleted from db.
                    videoService.deleteById(id);
                    // video's course progress should also be deleted
                    userCourseProgressService.deleteAllUserCourseProgressOfVideo(topicId);
                }
            }
        }
        String objectName = url.replace(GCP_BUCKET_URL, "");
        dataBucketUtil.deleteFile(objectName);
    }
}
