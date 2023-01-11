package it.plansoft.ecommerce.catalogupdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("catalog_update")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogUpdate {

    @Id
    String refId;

    Long timestamp;
}
