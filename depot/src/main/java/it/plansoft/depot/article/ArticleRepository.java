package it.plansoft.depot.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RepositoryEventHandler
public interface ArticleRepository extends CrudRepository<Article, Long> {

    final static Logger logger = LoggerFactory.getLogger(ArticleRepository.class);

    Optional<Article> findByName(String name);

}
