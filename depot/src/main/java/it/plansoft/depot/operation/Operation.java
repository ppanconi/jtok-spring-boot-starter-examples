package it.plansoft.depot.operation;

import com.jtok.spring.domainevent.DomainEvent;
import it.plansoft.depot.DepotDomainEventType;
import it.plansoft.depot.article.Article;
import it.plansoft.depot.handling.Handling;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation extends AbstractAggregateRoot {

    static final Logger logger = LoggerFactory.getLogger(Operation.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    Long timestamp;

    @Enumerated(EnumType.STRING)
    OperationStatus status;

    String refType;

    String refKey;

    @Column(unique = true)
    String refId;

    @OneToMany(mappedBy = "operation")
    List<Handling> handlings = new ArrayList<>();

    @Builder
    static Operation newWithRef(String refType, String refKey, String refId) {
        Operation operation = new Operation();

        operation.setRefId(refId);
        operation.setRefKey(refKey);
        operation.setRefType(refType);

        return operation;

    }

    static class OperationArticleItem {

        Article article;
        int quatity;

        public OperationArticleItem(Article article, int quatity) {
            this.article = article;
            this.quatity = quatity;
        }
    }

    public int place(List<OperationArticleItem> items) {

        Map<Article, Handling> articleHandlings = new HashMap<>();

        int placed = 0;
        HashMap<String, Object> details = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            OperationArticleItem item = items.get(i);

            if (articleHandlings.get(item.article) != null) {
                placed = -10;
                logger.warn("invalid operation handling duplicated article " + item.article);
                details.put(item.article.getName(), "duplicated article " + item.article.getName());
                break;
            }

            Optional<Handling> handling;

            if (item.quatity < 0) {
                handling = item.article.unload( - item.quatity, this);
            } else {
                handling = item.article.load(item.quatity, this);
            }

            if (handling.isPresent()) {
                articleHandlings.put(item.article, handling.get());
                details.put(item.article.getName(), handling.get().getQuantity());
                placed++;
            } else {
                placed = -1;
                details.put(item.article.getName(), "quantity not available");
                break;
            }
        }

        if (placed > 0) {
            /*
             * the operation can be placed
             */
            long ts = System.currentTimeMillis();
            setStatus(OperationStatus.PLACED);
            setTimestamp(ts);

            articleHandlings.forEach((article, handling) -> {
                article.getHandlingList().add(handling);
                article.setLastOperationTs(ts);
                getHandlings().add(handling);

            });

            registerEvent(DomainEvent.builderWithRef()
                    .key(refKey)
                    .domainEventType(DepotDomainEventType.OPERATION_HANDLING_PLACED)
                    .ref(refType)
                    .applicationPayload(new HashMap<String, Object>() {{
                        put("refKey", refKey);
                        put("status", OperationStatus.PLACED.name());
                        put("details", details);
                    }})
                    .build()
            );
        } else {
            /*
             * the operation should be refused
             */
            refuse(details);
        }

        return placed;
    }

    public void refuse(HashMap<String, Object> details) {
        long ts = System.currentTimeMillis();
        setStatus(OperationStatus.REFUSED);
        setTimestamp(ts);
        registerEvent(DomainEvent.builderWithRef()
                .key(refKey)
                .domainEventType(DepotDomainEventType.OPERATION_HANDLING_REFUSED)
                .ref(refType)
                .applicationPayload(new HashMap<String, Object>() {{
                    put("refKey", refKey);
                    put("status", OperationStatus.REFUSED.name());
                    put("details", details);
                }})
                .build()
        );
    }
}
