package it.plansoft.ecommerce;

import com.jtok.spring.domainevent.DomainEventType;
import com.jtok.spring.publisher.DomainEventTypesProvider;
import com.jtok.spring.publisher.EnableMongoDbDomainEventPublisher;
import com.jtok.spring.subscriber.EnableExternalDomainEventSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.List;


@SpringBootApplication
@EnableExternalDomainEventSubscriber
@EnableMongoDbDomainEventPublisher
@EnableMongoRepositories(basePackages = {"it.plansoft.ecommerce"} )
public class MongoEcommerceApplication {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }


    @Bean
    DomainEventTypesProvider domainEventTypesProvider() {
        return new DomainEventTypesProvider() {
            @Override
            public List<DomainEventType> provideDomainEventTypes() {
                return Arrays.asList(ECommerceDomainEvent.values());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MongoEcommerceApplication.class, args);
    }

}
