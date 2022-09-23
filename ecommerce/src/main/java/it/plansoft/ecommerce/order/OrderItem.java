package it.plansoft.ecommerce.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.plansoft.ecommerce.catalogarticle.CatalogArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @ManyToOne
    @JsonIgnore
    Order order;

    @ManyToOne
    CatalogArticle catalogArticle;

    int quantity;

    @Builder
    static OrderItem create(Order order, CatalogArticle catalogArticle, int quantity) {

        OrderItem item = new OrderItem();
        item.setCatalogArticle(catalogArticle);
        item.setOrder(order);
        item.setQuantity(quantity);

        return item;
    }

}
