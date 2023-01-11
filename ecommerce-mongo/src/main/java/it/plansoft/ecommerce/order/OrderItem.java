package it.plansoft.ecommerce.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    String catalogArticleId;
    String catalogArticleName;

    int quantity;

    @Builder
    static OrderItem create(String catalogArticleId, String catalogArticleName, int quantity) {

        OrderItem item = new OrderItem();
        item.setCatalogArticleId(catalogArticleId);
        item.setCatalogArticleName(catalogArticleName);
        item.setQuantity(quantity);

        return item;
    }

}
