package com.vinncorp.fast_learner.util.gcp;

import com.google.cloud.storage.*;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.dtos.file_manager.uploader.gcp.FileDto;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.util.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataBucketUtil {

    @Autowired
    private Storage storage;

    @Value("${gcp.bucket.url}")
    private String GCP_BUCKET_URL;

    @Value("${gcp.bucket.name}")
    private String GCP_BUCKET_NAME;


    public ResponseEntity<byte[]> download(String filename, FileType fileType) {
        log.info("Start file downloading process on GCS");
        // Blob blob = storage.get(GCP_BUCKET_NAME, filename);

        try {
            // Open a ReadableByteChannel to the object
            try (ReadableByteChannel readableByteChannel = storage.reader(BlobId.of(GCP_BUCKET_NAME, filename));
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                // Read the content of the file into a byte array
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (readableByteChannel.read(buffer) > 0) {
                    buffer.flip();
                    byteArrayOutputStream.write(buffer.array(), 0, buffer.limit());
                    buffer.clear();
                }

                byte[] content = byteArrayOutputStream.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                if(fileType == FileType.DOCS)
                    headers.setContentDispositionFormData("attachment", filename);
                else
                    headers.setContentDispositionFormData("inline", filename);

                return new ResponseEntity<>(content, headers, HttpStatus.OK);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //log.info("Fetched the file: "+ filename);
        //return blob.getContent();
    }

    public MultipartFile downloadFileOrVideo(String url, FileType fileType) throws BadRequestException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        try {
            // Parse the URL to extract bucket name and object name
            URI uri = new URI(url);
            String objectName = uri.getPath().substring(1); // removing the leading '/' from the path

            // Create BlobId from bucket name and object name
            BlobId blobId = BlobId.of(GCP_BUCKET_NAME, objectName.split("fastlearner-bucket/")[1]);

            try (ReadableByteChannel readableByteChannel = storage.reader(blobId);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                // Read the content of the file into a byte array
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (readableByteChannel.read(buffer) > 0) {
                    buffer.flip();
                    byteArrayOutputStream.write(buffer.array(), 0, buffer.limit());
                    buffer.clear();
                }
                String fileName = null;
                if(fileType == FileType.VIDEO)
                    fileName = "temp" + System.currentTimeMillis() + ".mp4";
                else if(fileType == FileType.DOCS)
                    fileName = "temp" + System.currentTimeMillis() + ".pdf";
                return new CustomMultipartFile(byteArrayOutputStream.toByteArray(), fileName, fileType == FileType.VIDEO ? "video/mp4" : "application/pdf");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Consider logging the exception
        }

        throw new BadRequestException("No file found with the provided URL.");
    }

    public FileDto uploadFile(UploadFileRequest request, String fileName, String contentType) throws BadRequestException {
        try{
            log.debug("Start file uploading process on GCS");
            byte[] fileData = request.getFile().getBytes();
//            byte[] fileData = Files.readAllBytes(convertFile(request.getFile().getBytes()).toPath());

            Bucket bucket = storage.get(GCP_BUCKET_NAME,Storage.BucketGetOption.fields());
            Blob blob = bucket.create(request.getFileType() + "/" + fileName, fileData, contentType);
            String link = GCP_BUCKET_URL + request.getFileType().name() + "/" + fileName;
            if(blob != null){
                log.debug("File successfully uploaded to GCS");
                return new FileDto(fileName, link, null);
            }
        }catch (Exception e){
            log.error("An error occurred while uploading data. Exception: ", e);
            throw new BadRequestException("An error occurred while storing data to GCS");
        }
        throw new BadRequestException("An error occurred while storing data to GCS");
    }

    public void deleteFile(String objectName) {
        log.info("Deleting a resource...");
        Bucket bucket = storage.get(GCP_BUCKET_NAME,Storage.BucketGetOption.fields());
        Blob blob = bucket.get(objectName);
        if(Objects.nonNull(blob)){
            blob.delete();
            log.info("Resources deleted successfully.");
        }else{
            log.info("No resource found with provided data.");
        }
    }
}
