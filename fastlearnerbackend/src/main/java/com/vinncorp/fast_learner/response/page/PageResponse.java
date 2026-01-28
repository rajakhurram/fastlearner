package com.vinncorp.fast_learner.response.page;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse {
    private Long previousPage;
    private Long currentPage;
    private Long nextPage;


}
