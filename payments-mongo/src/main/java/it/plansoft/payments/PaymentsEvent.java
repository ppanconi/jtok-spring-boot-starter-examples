package it.plansoft.payments;

import com.jtok.spring.domainevent.DomainEventTopicInfo;
import com.jtok.spring.domainevent.DomainEventType;

public enum PaymentsEvent implements DomainEventType {

    PAYMENTS_OPERATION_ADDED(PaymentsTopic.PAYMENT_OPERATION_TOPIC),
    PAYMENTS_OPERATION_REFUSED(PaymentsTopic.PAYMENT_OPERATION_TOPIC);

    private DomainEventTopicInfo topic;

    PaymentsEvent(DomainEventTopicInfo topic) {
        this.topic = topic;
    }

    @Override
    public DomainEventTopicInfo topic() {
        return topic;
    }
}
