package it.plansoft.payments.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.plansoft.payments.account.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "Operations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Version
    Long version;

    @NotNull
    BigDecimal amount;

    @NotNull
    Long timestamp;

    @NotNull
    @Column(unique = true)
    String refId;

    @Column(nullable = false)
    String refKey;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    Account account;
}
