package it.plansoft.depot.handling;

import it.plansoft.depot.article.Article;
import it.plansoft.depot.operation.Operation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "Handlings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Handling {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @ManyToOne
    Article article;

    @ManyToOne
    Operation operation;

    int quantity;

    long timestamp;

}
