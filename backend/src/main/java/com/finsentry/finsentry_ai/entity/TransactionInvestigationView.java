package com.finsentry.finsentry_ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Immutable
@Table(name = "transactions_investigation_view")
public class TransactionInvestigationView {
    @Id
    private Long id;
    private Integer step;
    private String type;
    private BigDecimal amount;

    @Column(name = "name_orig")
    private String nameOrig;
    @Column(name = "oldbalance_org")
    private BigDecimal oldbalanceOrg;
    @Column(name = "newbalance_orig")
    private BigDecimal newbalanceOrig;
    @Column(name = "name_dest")
    private String nameDest;
    @Column(name = "oldbalance_dest")
    private BigDecimal oldbalanceDest;
    @Column(name = "newbalance_dest")
    private BigDecimal newbalanceDest;

}
