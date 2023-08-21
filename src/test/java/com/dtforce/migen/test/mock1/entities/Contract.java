package com.dtforce.migen.test.mock1.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Audited
public class Contract
{

    @Id
    private UUID id;

    @Column
    private String name;

    @Column
    private BigDecimal total;

	@Audited
    @ManyToOne
    private ContractType type;

	@ColumnDefault("'test'")
	@Column(columnDefinition = "TEXT")
	private String note;

	@Audited
    @OneToMany(mappedBy = "contract", fetch = FetchType.EAGER)
    private List<ContractProperty> properties;

}
