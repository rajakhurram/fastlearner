package com.vinncorp.fast_learner.response.static_pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaticPagesResponse {
    private String slugify;
    private String type;
    private String content;
    private Date creationDate;

}
