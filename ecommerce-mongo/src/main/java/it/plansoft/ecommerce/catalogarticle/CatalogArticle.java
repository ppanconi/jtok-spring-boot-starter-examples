package it.plansoft.ecommerce.catalogarticle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

@Data
@Document("catalog_article")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogArticle {

    @Id
    String id;

    Long version;

    Long lastOperationTs;

    String name;

    @Nullable
    String description;

    int quantity;
}
