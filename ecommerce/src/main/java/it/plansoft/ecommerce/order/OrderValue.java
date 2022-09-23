package it.plansoft.ecommerce.order;

import lombok.Value;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Value
public class OrderValue {
    String customer;
    OrderStatus status;
    BigDecimal granTotal;
    String notes;
    List<OrderItemValue> itemValues = new ArrayList<>();
}
