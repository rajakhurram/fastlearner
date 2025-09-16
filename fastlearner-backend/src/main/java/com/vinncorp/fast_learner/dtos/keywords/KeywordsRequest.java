package com.vinncorp.fast_learner.dtos.keywords;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KeywordsRequest {
    private String courseTitle;
    private String sectionTitle;
    private List<String> topicNames;
}
