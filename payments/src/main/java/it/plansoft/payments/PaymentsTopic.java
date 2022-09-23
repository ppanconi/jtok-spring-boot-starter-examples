package it.plansoft.payments;

import com.jtok.spring.domainevent.DomainEventTopicInfo;

public enum PaymentsTopic implements DomainEventTopicInfo {

    PAYMENT_OPERATION_TOPIC("operation", 5, 1);

    private String topicName;
    private int topicPartitions;
    private int topicReplications;

    PaymentsTopic(String topicName, int topicPartitions, int topicReplications) {
        this.topicName = topicName;
        this.topicPartitions = topicPartitions;
        this.topicReplications = topicReplications;
    }

    @Override
    public String topicName() {
        return topicName;
    }

    @Override
    public int topicPartitions() {
        return topicPartitions;
    }

    @Override
    public int topicReplications() {
        return topicReplications;
    }
}
