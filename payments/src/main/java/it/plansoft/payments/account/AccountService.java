package it.plansoft.payments.account;

import com.jtok.spring.domainevent.DomainEvent;
import com.jtok.spring.subscriber.ExternalDomainEvent;
import it.plansoft.payments.PaymentsEvent;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountService(AccountRepository repository, ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public int addCharge(String userId, BigDecimal amount,
                         String refType, String refId, String refKey) {

        Optional<Account> byUserId = repository.findByUserId(userId);
        if ( byUserId.isPresent()) {
            Account account = byUserId.get();
            int added = account.charge(amount, refType, refId, refKey);
            repository.save(account);
            return added;
        } else {
            return -10;
        }
    }

    @Transactional
    public int addDeposit(String userId, BigDecimal amount,
                          String refType, String refId, String refKey) {

        Optional<Account> byUserId = repository.findByUserId(userId);
        if ( byUserId.isPresent()) {
            Account account = byUserId.get();
            int added = account.deposit(amount, refType, refId, refKey);
            repository.save(account);
            return added;
        } else {
            return -10;
        }
    }

    public Optional<Account> getByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Transactional
    @EventListener(condition = "#event.name == 'ecommerce.ORDER_TO_BE_PAYED'")
    public void handleECommerceOrderCreated(ExternalDomainEvent event) {

        JSONObject payload = event.getPayload();
        String userId = payload.getAsString("customer");
        String orderId = payload.getAsString("globalId");

        Number granTotal = payload.getAsNumber("granTotal");

        BigDecimal charge = new BigDecimal(granTotal.doubleValue()).multiply(new BigDecimal("-1.00"));

        int status = addCharge(userId, charge, event.getName(), event.getId(), orderId);

        if (status == -10) {
            log.warn("User " + userId + " not found on payments database");
            applicationEventPublisher.publishEvent(
                    DomainEvent.builderWithRef()
                            .key(event.getKey())
                            .domainEventType(PaymentsEvent.PAYMENTS_OPERATION_REFUSED)
                            .ref(event.getName())
                            .applicationPayload(new HashMap<String, Object>(){{
                                put("refKey", event.getKey());
                                put("operation", "charge");
                                put("amount", charge);
                            }})
                            .build()
            );
        }

    }

}