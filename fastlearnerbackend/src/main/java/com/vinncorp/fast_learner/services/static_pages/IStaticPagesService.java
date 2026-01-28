package com.vinncorp.fast_learner.services.static_pages;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.static_pages.StaticPages;
import com.vinncorp.fast_learner.response.static_pages.StaticPagesResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IStaticPagesService {
    Message<String> createStaticPage(StaticPages staticPage) throws InternalServerException;
    StaticPagesResponse getStaticPageBySlugify(String slugify) throws EntityNotFoundException;
}
