package it.plansoft.ecommerce.catalogupdate;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CatalogUpdateRepository extends CrudRepository<CatalogUpdate, String> {

//    Optional<CatalogUpdate> findByRefId(String refId);
}
