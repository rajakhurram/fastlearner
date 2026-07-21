package com.vinncorp.fast_learner.response.ai_grader.ai_class;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIClassResponse {
    private Long id;
    private String name;
    private String status;
    private Date creationDate;
    private Long assignmentCount;

}
