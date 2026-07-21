package com.vinncorp.fast_learner.controllers.pdf;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping(APIUrls.PDF_UTILITY)
@RequiredArgsConstructor
public class PDFUtilityController {

    @PostMapping(APIUrls.EXTRACT_DATA)
    public ResponseEntity<Message<String>> extractDataFromPDF(@RequestParam("file") MultipartFile file) throws BadRequestException, IOException {
            if (file.isEmpty() || !StringUtils.getFilenameExtension(file.getOriginalFilename()).equalsIgnoreCase("pdf")) {
                throw new BadRequestException("Only PDF files are allowed.");
            }

            // Extract text from the uploaded PDF
            String extractedText = extractTextFromPdf(file.getInputStream());

            var m = new Message<String>()
                    .setMessage("Extracted the data successfully.")
                    .setData(extractedText)
                    .setCode(HttpStatus.OK.name())
                    .setStatus(HttpStatus.OK.value());

            return ResponseEntity.ok(m);
    }

    private String extractTextFromPdf(InputStream pdfStream) {
        try (PDDocument document = PDDocument.load(pdfStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).replaceAll("\\s+", " ").trim();
        } catch (IOException e) {
            return "Failed to extract text: " + e.getMessage();
        }
    }
}
