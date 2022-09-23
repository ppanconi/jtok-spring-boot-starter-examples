package it.plansoft.ecommerce;

import com.jtok.spring.domainevent.DomainEventTopicInfo;
import com.jtok.spring.domainevent.DomainEventType;

public enum ECommerceDomainEvent implements DomainEventType {

    ORDER_CREATED(ECommerceTopicInfo.ORDER_LIFECYCLE_TOPIC),
    ORDER_CANCELLED(ECommerceTopicInfo.ORDER_LIFECYCLE_TOPIC),
    ORDER_DELETED(ECommerceTopicInfo.ORDER_LIFECYCLE_TOPIC),
    ORDER_TO_BE_PAYED(ECommerceTopicInfo.ORDER_TO_BE_PAYED_TOPIC);

    private DomainEventTopicInfo topic;

    ECommerceDomainEvent(DomainEventTopicInfo topic) {
        this.topic = topic;
    }

    @Override
    public DomainEventTopicInfo topic() {
        return topic;
    }
}
