package com.vinncorp.fast_learner.response.ai_grader.ai_class;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIClass {
    private Long id;
    private String name;
    private Date creationDate;
    private Long instructorId;
    private String status;
}
