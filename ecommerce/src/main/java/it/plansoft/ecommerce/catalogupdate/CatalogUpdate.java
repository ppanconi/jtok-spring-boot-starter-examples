package it.plansoft.ecommerce.catalogupdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogUpdate {

    @Id
    String refId;

    Long timestamp;
}
