package it.plansoft.ecommerce.order;

import lombok.Value;

@Value
public class OrderItemValue {

    String article;
    Integer quantity;
}
