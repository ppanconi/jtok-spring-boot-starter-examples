package it.plansoft.payments.account;

import com.jtok.spring.domainevent.DomainEvent;
import it.plansoft.payments.PaymentsEvent;
import it.plansoft.payments.operation.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "Accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account extends AbstractAggregateRoot {

    private static final Logger log = LoggerFactory.getLogger(Account.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Version
    Long version;

    @NotNull
    @Column(unique = true)
    String userId;

    @NotNull
    Long lastOperationTs = System.currentTimeMillis();

    String notes;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    List<Operation> operations = new ArrayList<>();

    public BigDecimal getBalance() {
        return balance();
    }

    /**
     * business methods
     */

    public Optional<Operation> operationByReference(String refId) {
        return getOperations().stream().filter(operation -> operation.getRefId().equals(refId)).findFirst();
    }

    public BigDecimal balance() {
        return getOperations().stream().map(operation -> operation.getAmount())
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    public int charge(BigDecimal amount, String refType, String refId, String refKey) {

        if (operationByReference(refId).isPresent()) {
            log.warn("duplicated account_operation elaboration, ignored in at-least-once semantic");
            return 0;
        }

        if ( balance().add(amount).compareTo(BigDecimal.ZERO)  >= 0  ) {

            long ts = System.currentTimeMillis();
            getOperations().add(Operation.builder()
                    .account(this)
                    .amount(amount)
                    .refId(refId)
                    .refKey(refKey)
                    .timestamp(ts)
                    .build());

            registerEvent(DomainEvent.builderWithRef()
                    .key(refKey)
                    .domainEventType(PaymentsEvent.PAYMENTS_OPERATION_ADDED)
                    .ref(refType)
                    .applicationPayload(new HashMap<String, Object>(){{
                        put("refKey", refKey);
                        put("operation", "charge");
                        put("amount", amount);
                    }})
                    .build()
            );

            setLastOperationTs(ts);

            return 1;
        } else {

            registerEvent(DomainEvent.builderWithRef()
                    .key(refKey)
                    .domainEventType(PaymentsEvent.PAYMENTS_OPERATION_REFUSED)
                    .ref(refType)
                    .applicationPayload(new HashMap<String, Object>(){{
                        put("refKey", refKey);
                        put("operation", "charge");
                        put("amount", amount);
                    }})
                .build()
            );

            setLastOperationTs(System.currentTimeMillis());

            return -1;
        }
    }

    public int deposit(BigDecimal amount, String refType, String refId, String refKey) {

        if (operationByReference(refId).isPresent()) {
            log.warn("duplicated account_operation elaboration, ignored in at-least-once semantic");
            return 0;
        }

        long ts = System.currentTimeMillis();
        getOperations().add(Operation.builder()
                .account(this)
                .amount(amount)
                .refId(refId)
                .refKey(refKey)
                .timestamp(ts)
                .build());

        registerEvent(DomainEvent.builderWithRef()
                .key(refKey)
                .domainEventType(PaymentsEvent.PAYMENTS_OPERATION_ADDED)
                .ref(refType)
                .applicationPayload(new HashMap<String, Object>(){{
                    put("refKey", refKey);
                    put("operation", "deposit");
                    put("amount", amount);
                }})
                .build()
        );

        setLastOperationTs(ts);
        return 1;
    }

}
