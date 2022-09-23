package it.plansoft.depot.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@RepositoryEventHandler
@Component
public class ArticleCrudHandler {

    final Logger logger = LoggerFactory.getLogger(ArticleCrudHandler.class);

    final private ArticleRepository repository;

    public ArticleCrudHandler(ArticleRepository repository) {
        this.repository = repository;
    }

    @HandleBeforeCreate
    public void onArticleCreation(Article article) {
        logger.info("Creating new Article " + article);
        article.create();
    }

    @HandleBeforeDelete
    public void onArticleDelete(Article article) {
        logger.info("Deleting Article " + article);
        article.delete();
        repository.save(article);
    }

    @HandleBeforeSave
    public void onArticleUpdate(Article article) {
        logger.info("Updating Article " + article);
        article.update();
    }
}
