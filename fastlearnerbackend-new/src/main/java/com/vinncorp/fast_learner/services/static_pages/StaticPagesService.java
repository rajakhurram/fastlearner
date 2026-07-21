package com.vinncorp.fast_learner.services.static_pages;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.static_pages.StaticPages;
import com.vinncorp.fast_learner.repositories.static_pages.StaticPagesRepository;
import com.vinncorp.fast_learner.response.static_pages.StaticPagesResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StaticPagesService implements IStaticPagesService{
    private final StaticPagesRepository staticPagesRepository;

    @Autowired
    public StaticPagesService(StaticPagesRepository staticPagesRepository) {
        this.staticPagesRepository = staticPagesRepository;
    }

    @Override
    public Message<String> createStaticPage(StaticPages staticPage) throws InternalServerException {
        try {
            StaticPages savedPage = staticPagesRepository.save(staticPage);

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Static Page Created Successfully.");

        } catch (Exception e) {
            throw new InternalServerException("StaticPage " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, e);
        }
    }


    @Override
    public StaticPagesResponse getStaticPageBySlugify(String slugify) throws EntityNotFoundException {
        StaticPages staticPage = staticPagesRepository.findBySlugify(slugify)
                .orElseThrow(() -> new EntityNotFoundException("Static page not found with slugify: " + slugify));
        return new StaticPagesResponse(
                staticPage.getSlugify(),
                staticPage.getType(),
                staticPage.getContent(),
                staticPage.getCreationDate()
        );
    }
}
