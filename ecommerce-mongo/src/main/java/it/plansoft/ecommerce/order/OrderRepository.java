package it.plansoft.ecommerce.order;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrderRepository extends CrudRepository<Order, String> {

    Optional<Order> findByGlobalId(String globalId);
}
