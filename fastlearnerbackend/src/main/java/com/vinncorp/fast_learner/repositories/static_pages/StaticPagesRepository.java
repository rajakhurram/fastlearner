package com.vinncorp.fast_learner.repositories.static_pages;

import com.vinncorp.fast_learner.models.static_pages.StaticPages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaticPagesRepository extends JpaRepository<StaticPages,Long> {
    Optional<StaticPages> findBySlugify(String slugify);

}
