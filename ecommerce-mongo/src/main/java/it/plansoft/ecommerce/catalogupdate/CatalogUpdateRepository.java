package it.plansoft.ecommerce.catalogupdate;

import org.springframework.data.repository.CrudRepository;

public interface CatalogUpdateRepository extends CrudRepository<CatalogUpdate, String> {

//    Optional<CatalogUpdate> findByRefId(String refId);
}
