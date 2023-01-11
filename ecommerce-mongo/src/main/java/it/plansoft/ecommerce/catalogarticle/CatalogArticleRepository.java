package it.plansoft.ecommerce.catalogarticle;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CatalogArticleRepository extends CrudRepository<CatalogArticle, String> {

    Optional<CatalogArticle> findByName(String name);
}
