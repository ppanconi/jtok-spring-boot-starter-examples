package it.plansoft.ecommerce.order;

import com.jtok.spring.subscriber.ExternalDomainEvent;
import it.plansoft.ecommerce.catalogarticle.CatalogArticle;
import it.plansoft.ecommerce.catalogarticle.CatalogArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final CatalogArticleRepository catalogArticleRepository;

    public OrderService(OrderRepository repository, CatalogArticleRepository catalogArticleRepository) {
        this.repository = repository;
        this.catalogArticleRepository = catalogArticleRepository;
    }

    private List<OrderItem> mapItems(Order order, List<OrderItemValue> values) {
        return values.stream().map(orderItemValue ->
            OrderItem.builder()
                    .catalogArticle(catalogArticleRepository.findByName(orderItemValue.getArticle()).get())
                    .order(order)
                    .quantity(orderItemValue.getQuantity())
                    .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public Order createOrder(OrderValue value) {

        String globalId = UUID.randomUUID().toString();

        Order order = new Order();

        order.setGlobalId(globalId);
        order.setStatus(OrderStatus.CREATED);
        order.setCustomer(value.getCustomer());
        order.setCurrency(Currency.getInstance("EUR"));
        order.setGranTotal(value.getGranTotal());
        order.setNotes(value.getNotes());
        order.setItems(mapItems(order, value.getItemValues()));

        repository.save(order);

        return order;
    }

    @Transactional
    public Order checkoutOrder(Long orderId) {

        Order order = repository.findById(orderId).get();

        if (order.getStatus() == OrderStatus.APPROVED ||
                order.getStatus() == OrderStatus.PAYMENT_REFUSED) {
            order.setStatus(OrderStatus.TO_BE_PAYED);
            repository.save(order);
        } else {
            throw new IllegalStateException("order state");
        }

        return order;
    }

    @Transactional
    public Order approveOrder(String orderGlobalId) {

        Order order = repository.findByGlobalId(orderGlobalId).get();
        if (order.getStatus() == OrderStatus.CREATED) {
            order.setStatus(OrderStatus.APPROVED);
            repository.save(order);
        }
        return order;
    }

    @Transactional
    public Order rejectOrder(String orderGlobalId) {

        Order order = repository.findByGlobalId(orderGlobalId).get();

        if (order.getStatus() == OrderStatus.CREATED) {
            order.setStatus(OrderStatus.REJECTED);
            repository.save(order);
        }
        return order;
    }

    @Transactional
    @EventListener(condition = "#event.name == 'depot.OPERATION_HANDLING_PLACED' && #event.refName =='ecommerce.ORDER_CREATED'")
    public void handleDepotOrderAccepted(ExternalDomainEvent event) {
        approveOrder(event.getPayload().getAsString("refKey"));
    }

    @Transactional
    @EventListener(condition = "#event.name == 'depot.OPERATION_HANDLING_REFUSED' && #event.refName =='ecommerce.ORDER_CREATED'")
    public void handleDepotOrderRejected(ExternalDomainEvent event) {
        rejectOrder(event.getPayload().getAsString("refKey"));
    }

    @Transactional
    @EventListener(condition = "#event.name == 'payments.PAYMENTS_OPERATION_REFUSED' && #event.refName =='ecommerce.ORDER_TO_BE_PAYED'")
    public void handlePaymentRefused(ExternalDomainEvent event) {

        logger.info("handle event " + event);

        Optional<Order> byGlobalId = repository.findByGlobalId(event.getKey());
        if (byGlobalId.isPresent()) {

            Order order = byGlobalId.get();
            if (order.getStatus() == OrderStatus.TO_BE_PAYED) {
                order.setStatus(OrderStatus.PAYMENT_REFUSED);
                repository.save(order);
            }

        } else {
            // !!! report to evidence !!!
            // in production probably you should use
            // a alerting system with dead leter topic
            throw new RuntimeException("ref event but no present key");
        }
    }

    @Transactional
    @EventListener(condition = "#event.name == 'payments.PAYMENTS_OPERATION_ADDED' && #event.refName =='ecommerce.ORDER_TO_BE_PAYED'")
    public void handlePayment(ExternalDomainEvent event) {

        logger.info("handle event " + event);

        Optional<Order> byGlobalId = repository.findByGlobalId(event.getKey());
        if (byGlobalId.isPresent()) {

            Order order = byGlobalId.get();
            if (order.getStatus() == OrderStatus.TO_BE_PAYED) {

                order.setStatus(OrderStatus.PAYED);
                repository.save(order);
            }

        } else {
            // !!! report to evidence !!!
            // in production probably you should use
            // a alerting system with dead leter topic
            throw new RuntimeException("ref event but no present key");
        }
    }
}
