package it.plansoft.payments.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Operation {

    Long version;

    @NotNull
    BigDecimal amount;

    @NotNull
    Long timestamp;

    @NotNull
    String refId;

    String refKey;

}
