package com.vinncorp.fast_learner.dtos.docs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {

    private Integer id;
    private String name;
    private String url;
    private String summary;
}
