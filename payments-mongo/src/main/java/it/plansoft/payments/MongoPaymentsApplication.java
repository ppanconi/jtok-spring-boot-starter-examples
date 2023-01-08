package it.plansoft.payments;

import com.jtok.spring.domainevent.DomainEventType;
import com.jtok.spring.publisher.DomainEventTypesProvider;
import com.jtok.spring.publisher.EnableMongoDbDomainEventPublisher;
import com.jtok.spring.subscriber.EnableExternalDomainEventSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableExternalDomainEventSubscriber
@EnableMongoDbDomainEventPublisher
@EnableMongoRepositories(basePackages = {"it.plansoft.payments"} )
public class MongoPaymentsApplication {

    @Bean
    public DomainEventTypesProvider domainEventTypesProvider() {
        return new DomainEventTypesProvider() {
            @Override
            public List<DomainEventType> provideDomainEventTypes() {
                return Arrays.asList(PaymentsEvent.values());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MongoPaymentsApplication.class, args);
    }

}
