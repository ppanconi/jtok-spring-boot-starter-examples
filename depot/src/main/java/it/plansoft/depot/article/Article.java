package it.plansoft.depot.article;

import com.jtok.spring.domainevent.DomainEvent;
import it.plansoft.depot.DepotDomainEventType;
import it.plansoft.depot.handling.Handling;
import it.plansoft.depot.operation.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "Articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article extends AbstractAggregateRoot {

    private static final Logger log = LoggerFactory.getLogger(Article.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Version
    Long version;

    Long lastOperationTs;

    @Column(unique = true)
    String name;

    @Nullable
    String description;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    List<Handling> handlingList = new ArrayList<>();

    public int getStockQuantity() {
        return getHandlingList().stream().map(handling -> handling.getQuantity())
                .reduce(0, (a, b) -> a + b);
    }

    /**
     * Create an article handling operation to reduce stock
     *
     * @param quantity positive quatity to reduce
     * @return
     */
    public Optional<Handling> unload(int quantity, Operation operation) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("non positive quantity unload " + quantity);
        }

        if ( getStockQuantity() - quantity  >= 0  ) {

            long ts = System.currentTimeMillis();

            return Optional.of(
                    Handling.builder()
                        .article(this)
                        .operation(operation)
                        .quantity( - quantity)
                        .timestamp(ts)
                    .build()
            );

        } else {
            return Optional.empty();
        }
    }

    /**
     * Create and article handling operation to increase stock
     *
     * @param qunatity positive quatity to add
     * @return
     */
    public Optional<Handling> load(int qunatity, Operation operation) {

        if (qunatity <= 0) {
            throw new IllegalArgumentException("non positive quantity load " + qunatity);
        }

        long ts = System.currentTimeMillis();
        return Optional.of(Handling.builder()
                        .article(this)
                        .operation(operation)
                        .quantity(qunatity)
                        .timestamp(ts)
                    .build()
                );
    }

    public Article create() {
        registerEvent(
                DomainEvent.builder()
                    .key(getName())
                    .domainEventType(DepotDomainEventType.ARTICLE_CREATED)
                    .applicationPayload(proviceApplicationPayload()).build()
        );
        return this;
    }

    public Article update() {
        registerEvent(
                DomainEvent.builder()
                        .key(getName())
                        .domainEventType(DepotDomainEventType.ARTICLE_UPDATE)
                        .applicationPayload(proviceApplicationPayload()).build()
        );
        return this;
    }

    public Article delete() {
        registerEvent(
                DomainEvent.builder()
                        .key(getName())
                        .domainEventType(DepotDomainEventType.ARTICLE_DELETED)
                        .applicationPayload(proviceApplicationPayload()).build()
        );
        return this;
    }

    private HashMap<String, Object> proviceApplicationPayload() {
        return new HashMap<String, Object>() {{
            put("name", getName());
            put("description", getDescription());
        }};
    }


}
