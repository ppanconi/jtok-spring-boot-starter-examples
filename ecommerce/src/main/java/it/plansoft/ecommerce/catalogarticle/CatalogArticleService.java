package it.plansoft.ecommerce.catalogarticle;

import com.jtok.spring.subscriber.ExternalDomainEvent;
import it.plansoft.ecommerce.catalogupdate.CatalogUpdate;
import it.plansoft.ecommerce.catalogupdate.CatalogUpdateRepository;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CatalogArticleService {

    static final Logger logger = LoggerFactory.getLogger(CatalogArticleService.class);

    private final CatalogArticleRepository catalogArticleRepository;
    private final CatalogUpdateRepository catalogUpdateRepository;

    public CatalogArticleService(CatalogArticleRepository catalogArticleRepository, CatalogUpdateRepository catalogUpdateRepository) {
        this.catalogArticleRepository = catalogArticleRepository;
        this.catalogUpdateRepository = catalogUpdateRepository;
    }

    @Transactional
    @EventListener(condition = "#event.name == 'depot.ARTICLE_CREATED'")
    public void handleDepotArticleCreation(ExternalDomainEvent event) {

        String name = event.getPayload().getAsString("name");

        Optional<CatalogArticle> byName = catalogArticleRepository.findByName(name);

        if (! byName.isPresent()) {
            CatalogArticle catalogArticle = CatalogArticle.builder()
                    .name(name)
                    .description(event.getPayload().getAsString("description"))
                    .build();

            catalogArticleRepository.save(catalogArticle);
        }
    }

    @Transactional
    @EventListener(condition = "#event.name == 'depot.ARTICLE_DELETED'")
    public void handleDepotArticleDeletion(ExternalDomainEvent event) {

        String name = event.getPayload().getAsString("name");

        Optional<CatalogArticle> byName = catalogArticleRepository.findByName(name);

        if (byName.isPresent()) {
            catalogArticleRepository.delete(byName.get());
        }
    }

    @Transactional
    @EventListener(condition = "#event.name == 'depot.ARTICLE_UPDATE'")
    public void handleDepotArticleUpdate(ExternalDomainEvent event) {

        String name = event.getPayload().getAsString("name");

        Optional<CatalogArticle> byName = catalogArticleRepository.findByName(name);

        if (byName.isPresent()) {
            CatalogArticle catalogArticle = byName.get();
            catalogArticle.setDescription(event.getPayload().getAsString("description"));
            catalogArticleRepository.save(catalogArticle);
        } else {
            // in production report this
            // on alerting system
            logger.warn("depot.ARTICLE_UPDATE on not present CatalogArticle: " + name );
        }
    }

    @Transactional
    @EventListener(condition = "#event.name == 'depot.OPERATION_HANDLING_PLACED'")
    public void handleDepotHandlingPlaced(ExternalDomainEvent event) {

        Optional<CatalogUpdate> byRefId = catalogUpdateRepository.findById(event.getId());

        if ( ! byRefId.isPresent()) {

            CatalogUpdate catalogUpdate = CatalogUpdate.builder()
                    .refId(event.getId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            JSONObject details = (JSONObject)event.getPayload().get("details");

            details.keySet().forEach(articleName -> {
                int articleQuantityUpdate = details.getAsNumber(articleName).intValue();

                Optional<CatalogArticle> byName = catalogArticleRepository.findByName(articleName);
                if (byName.isPresent()) {

                    CatalogArticle catalogArticle = byName.get();
                    catalogArticle.setQuantity( catalogArticle.getQuantity() + articleQuantityUpdate);
                    catalogArticleRepository.save(catalogArticle);

                } else {
                    throw new RuntimeException("depot.OPERATION_HANDLING_PLACED on not present catalog article " + articleName);
                }
            });

            catalogUpdateRepository.save(catalogUpdate);

        }

    }

}
