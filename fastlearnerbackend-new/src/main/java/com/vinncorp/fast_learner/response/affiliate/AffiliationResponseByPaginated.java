package com.vinncorp.fast_learner.response.affiliate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliationResponseByPaginated {
    private List<AffiliationResponse> affiliates;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int pages;
}
