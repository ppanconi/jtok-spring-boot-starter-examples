package it.plansoft.depot;

import com.jtok.spring.domainevent.DomainEventTopicInfo;
import com.jtok.spring.domainevent.DomainEventType;

public enum DepotDomainEventType implements DomainEventType {

    ARTICLE_CREATED(DepotTopic.ARTICLE_TOPIC),
    ARTICLE_DELETED(DepotTopic.ARTICLE_TOPIC),
    ARTICLE_UPDATE(DepotTopic.ARTICLE_TOPIC),

    OPERATION_HANDLING_PLACED(DepotTopic.OPERATION_HANDLING_TOPIC),
    OPERATION_HANDLING_REFUSED(DepotTopic.OPERATION_HANDLING_TOPIC),
    ;

    private DomainEventTopicInfo topic;

    DepotDomainEventType(DomainEventTopicInfo topic) {
        this.topic = topic;
    }

    @Override
    public DomainEventTopicInfo topic() {
        return topic;
    }
}
