package it.plansoft.depot.operation;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OperationRepository extends CrudRepository<Operation, Long> {

    Optional<Operation> findByRefId(String refId);
}
