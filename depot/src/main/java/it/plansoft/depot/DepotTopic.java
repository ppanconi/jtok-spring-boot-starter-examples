package it.plansoft.depot;

import com.jtok.spring.domainevent.DomainEventTopicInfo;

public enum DepotTopic implements DomainEventTopicInfo {

    ARTICLE_TOPIC("article", 3, 1),
    OPERATION_HANDLING_TOPIC("handling", 7, 1);

    private String topicName;
    private int topicPartitions;
    private int topicReplications;

    DepotTopic(String topicName, int topicPartitions, int topicReplications) {
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
