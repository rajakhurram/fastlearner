package com.vinncorp.fast_learner.controllers.static_pages;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.static_pages.StaticPages;
import com.vinncorp.fast_learner.response.static_pages.StaticPagesResponse;
import com.vinncorp.fast_learner.services.static_pages.IStaticPagesService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(APIUrls.STATIC_PAGE)
@RequiredArgsConstructor
public class StaticPagesController {

    private final IStaticPagesService staticPagesService;

    @PostMapping(APIUrls.CREATE_STATIC_PAGE)
    public ResponseEntity<Message<String>> createStaticPage(@RequestBody StaticPages staticPage) throws InternalServerException {
        Message<String> response = staticPagesService.createStaticPage(staticPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping(APIUrls.GET_STATIC_PAGE)
    public ResponseEntity<StaticPagesResponse> getStaticPage(@PathVariable String slugify) throws EntityNotFoundException {
        StaticPagesResponse response = staticPagesService.getStaticPageBySlugify(slugify);
        return ResponseEntity.ok(response);
    }
}
