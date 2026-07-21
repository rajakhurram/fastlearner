package com.vinncorp.fast_learner.mock.document;

import com.vinncorp.fast_learner.dtos.docs.DocumentSummary;
import com.vinncorp.fast_learner.mock.article.ArticleTestData;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import com.vinncorp.fast_learner.models.docs.Document;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class DocumentTestData {

    public static Document document() throws IOException {
        return Document.builder()
                .id(1L)
                .summary("Document summary")
                .article(ArticleTestData.article())
                .url("http://example.com/document.pdf")
                .name("Document-Name")
                .video(new Video())
                .build();
    }

    public static DocumentSummary documentSummary() {
        return DocumentSummary.builder()
                .summary("Document summary")
                .build();
    }

    public static DocumentSummaryResponse documentSummaryResponse() {
        return DocumentSummaryResponse.builder()
                .code(HttpStatus.OK.name())
                .status(HttpStatus.OK.value())
                .message("Successfully fetch document summary.")
                .data(documentSummary())
                .build();
    }
}
