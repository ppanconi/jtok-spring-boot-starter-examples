package it.plansoft.depot.operation;

import com.jtok.spring.subscriber.ExternalDomainEvent;
import it.plansoft.depot.article.Article;
import it.plansoft.depot.article.ArticleRepository;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class OperationService {

    final Logger logger = LoggerFactory.getLogger(OperationService.class);

    private final OperationRepository operationRepository;
    private final ArticleRepository articleRepository;

    public OperationService(OperationRepository operationRepository, ArticleRepository articleRepository) {
        this.operationRepository = operationRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public int place(OperationValue value) {

        Optional<Operation> byRefId = operationRepository.findByRefId(value.getRefId());

        if (byRefId.isPresent()) {
            logger.warn("duplicated depot operation elaboration, ignored in at-least-once semantic");
            return 0;
        }

        Operation operation = Operation.builder()
                .refId(value.getRefId())
                .refType(value.getRefType())
                .refKey(value.getRefKey())
                .build();

        List<Operation.OperationArticleItem> items = new ArrayList<>();
        HashMap<String, Object> details = new HashMap<>();

        boolean allArticlesPresent = true;
        for (int i = 0; i < value.getDetails().size(); i++) {
            OperationDetailValue detail = value.getDetails().get(i);
            Optional<Article> articleByName = articleRepository.findByName(detail.getArticle());
            if ( ! articleByName.isPresent()) {
                allArticlesPresent = false;
                details.put(detail.getArticle(), "not present");
            } else {
                items.add(new Operation.OperationArticleItem(
                        articleByName.get(),
                        detail.getQuantity()
                ));
            }
        }

        int status;
        if (allArticlesPresent) {
            status = operation.place(items);
        } else {
            status = -50;
            operation.refuse(details);
        }

        operationRepository.save(operation);
        return status;
    }

    @Transactional
    @EventListener(condition = "#event.name == 'ecommerce.ORDER_CREATED'")
    public void handleECommerceOrderCreated(ExternalDomainEvent event) {

        List<OperationDetailValue> details = new ArrayList<>();

        JSONObject items = (JSONObject) event.getPayload().get("items");
        items.keySet().forEach(article ->
                details.add(new OperationDetailValue(article, - items.getAsNumber(article).intValue())));

        OperationValue operationValue = new OperationValue(
                event.getName(),
                event.getKey(),
                event.getId(),
                details
        );

        place(operationValue);
    }
}
