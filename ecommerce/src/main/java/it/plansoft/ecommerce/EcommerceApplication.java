package it.plansoft.ecommerce;

import com.jtok.spring.domainevent.DomainEventType;
import com.jtok.spring.publisher.DomainEventTypesProvider;
import com.jtok.spring.publisher.EnableDomainEventPublisher;
import com.jtok.spring.subscriber.EnableExternalDomainEventSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;
import java.util.List;


@SpringBootApplication
@EnableDomainEventPublisher
@EnableExternalDomainEventSubscriber
@EntityScan({"it.plansoft.ecommerce"})
@EnableJpaRepositories
public class EcommerceApplication {

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
        SpringApplication.run(EcommerceApplication.class, args);
    }

}
