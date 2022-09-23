package it.plansoft.payments;

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
@EnableExternalDomainEventSubscriber
@EnableDomainEventPublisher
@EntityScan({"it.plansoft.payments"})
@EnableJpaRepositories
public class PaymentsApplication {

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
        SpringApplication.run(PaymentsApplication.class, args);
    }

}
