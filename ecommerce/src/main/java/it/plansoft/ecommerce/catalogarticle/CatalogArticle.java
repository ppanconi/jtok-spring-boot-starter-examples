package it.plansoft.ecommerce.catalogarticle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Version
    Long version;

    Long lastOperationTs;

    @Column(unique = true)
    String name;

    @Nullable
    String description;

    int quantity;
}
